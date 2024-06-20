package beans;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import mbeans.Percent;
import mbeans.PercentMBean;
import mbeans.Points;
import mbeans.PointsMBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.management.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@ManagedBean(name = "table")
@SessionScoped
public class ResultTable implements Serializable {
    private static final long serialVersionUID = 1L;
    private static Connection connection;
    @Getter
    @Setter
    private List<Point> results;

    private PointsMBean pointCounter;
    private PercentMBean hitPercent;

    private final Lock lock = new ReentrantLock();
    private static final Logger LOGGER = LogManager.getLogger(ResultTable.class);

    @PostConstruct
    public void init() {
        try {
            Context context = new InitialContext();
            try {
                DataSource ds = (DataSource) context.lookup("java:jboss/datasources/ExampleDS");
                connection = ds.getConnection();
            } catch (Exception e) {
                connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "123456sG");
            }
            LOGGER.info("server connected to the database");
            createTable();
            getDataFromTable();
            try {
                MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                ObjectName pointCounterName = new ObjectName("beans.mbeans:type=Points");
                pointCounter = new Points(this);
                mbs.registerMBean(pointCounter,pointCounterName);

                NotificationListener listener = ((notification, handback) -> System.out.println("Received notification: " + notification.getMessage()));
                mbs.addNotificationListener(pointCounterName, listener, null, null);

                hitPercent = new Percent(pointCounter);
                ObjectName hitPercentName = new ObjectName("beans.mbeans:type=Percent");
                StandardMBean hitPercentMBean = new StandardMBean(hitPercent, PercentMBean.class);
                mbs.registerMBean(hitPercentMBean,hitPercentName);
            } catch (MalformedObjectNameException | NotCompliantMBeanException | InstanceAlreadyExistsException | MBeanRegistrationException | InstanceNotFoundException e) {
                e.printStackTrace();
            }
        } catch (SQLException | IOException | NamingException e) {
            LOGGER.error(e.getMessage());
            System.exit(-1);
        }
    }

    private void createTable() throws SQLException, IOException {
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS HITS"
                + "(X DOUBLE PRECISION, "
                + "Y DOUBLE PRECISION, "
                + "R DOUBLE PRECISION,"
                + "HIT BOOLEAN, "
                + "ATTEMPT_TIME BIGINT,"
                + "EXECUTION_TIME DOUBLE PRECISION);");

        LOGGER.info("table created");
    }

    private void getDataFromTable() throws SQLException, IOException {
        results = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM HITS");
        while (rs.next()) {
            try {
                lock.lock();
                results.add(new Point(rs.getDouble("X"), rs.getDouble("Y"),
                        rs.getDouble("R"), rs.getBoolean("HIT"), rs.getLong("ATTEMPT_TIME"),
                        rs.getLong("EXECUTION_TIME")));
            } finally {
                lock.unlock();
            }
        }
        LOGGER.info("results were read");
    }


    public void addPoint(Point row) {
        try {
            pointCounter.incrementTotalPoints();
            if (!row.checkHit()) {
                pointCounter.infoMissedPoints();
                pointCounter.incrementMissedPoints();
            }
            PreparedStatement statement = connection.prepareStatement("INSERT INTO HITS "
                    + "(X, Y, R, HIT, ATTEMPT_TIME, EXECUTION_TIME) VALUES (?, ?, ?, ?, ?, ?)");
            statement.setDouble(1, row.getX());
            statement.setDouble(2, row.getY());
            statement.setDouble(3, row.getR());
            statement.setBoolean(4, row.isHit());
            statement.setLong(5, row.getAttemptTime());
            statement.setDouble(6, row.getExecutionTime());
            statement.execute();
            try {
                lock.lock();
                results.add(row);
            } finally {
                lock.unlock();
            }
            LOGGER.info("a new entry has been added to the database");
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void clear() {
        try {
            Statement statement = connection.createStatement();
            statement.execute("TRUNCATE TABLE HITS");
            try {
                lock.lock();
                results.clear();
            } finally {
                lock.unlock();
            }
            pointCounter.resetPoints();
            LOGGER.info("the table has been cleared");
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public String getX() {
        return new Gson().toJson(getResults().stream().map(Point::getX).collect(Collectors.toList()));
    }

    public String getY() {
        return new Gson().toJson(getResults().stream().map(Point::getY).collect(Collectors.toList()));
    }

    public String getR() {
        return new Gson().toJson(getResults().stream().map(Point::getR).collect(Collectors.toList()));
    }

    public String getHit() {
        return new Gson().toJson(getResults().stream().map(Point::isHit).collect(Collectors.toList()));
    }
}
