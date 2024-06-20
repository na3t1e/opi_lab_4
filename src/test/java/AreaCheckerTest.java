import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AreaCheckerTest {
    public static boolean isInArea(double x, double y, double r) {
        boolean hit = ((x >= - r / 2) && (x <= 0) && (y >= 0) && (y <= r)) ||
        ((x <= 0) && (x >= -r / 2) && (y <= 0) && (y >= -r - 2 * x)) ||
        ((x >= 0) && (y >= 0) && (x * x + y * y <= r * r ));
        return hit;
    }

    @Test
    public void testIsInAreaForRectangle() {
        // Inside the rectangle
        assertTrue(isInArea(-4, 1, 12));
        assertTrue(isInArea(-5, 10, 11));
        assertTrue(isInArea(-2, 5, 9));

        // Outside the rectangle
        assertFalse(isInArea(-6, 1, 10));
        assertFalse(isInArea(-1, 11, 9));

        // On the borders
        assertTrue(isInArea(-2, 0, 4));
        assertTrue(isInArea(-7, 14, 14));
        assertTrue(isInArea(0, 0, 2));
        assertTrue(isInArea(0, 1, 1));
    }

    @Test
    public void testIsInAreaForSemiCircle() {
        // Inside the semi-circle
        assertTrue(isInArea(3, 0, 3));
        assertTrue(isInArea(0, 4, 4));
        assertTrue(isInArea(70, 70, 140));
        assertTrue(isInArea(0, 0, 10));

        // Outside the semi-circle
        assertFalse(isInArea(21, 21, 21));
        assertFalse(isInArea(0, 11, 10));
        assertFalse(isInArea(11, 0, 10));

        // On the borders
        assertTrue(isInArea(2, 0, 2));
        assertTrue(isInArea(0, 8, 8));
    }

    @Test
    public void testIsInAreaForTriangle() {
        // Inside the triangle
        assertTrue(isInArea(-5, 0, 10));
        assertTrue(isInArea(0, -20, 20));
        assertTrue(isInArea(-1, -1, 8));
        assertTrue(isInArea(-2.5, -2.5, 10));

        // On the borders
        assertTrue(isInArea(-2.5, 0, 5));
        assertTrue(isInArea(0, -1, 1));

        // Outside the triangle
        assertFalse(isInArea(-5, -0.1, 10));
        assertFalse(isInArea(-6, -2, 11));
        assertFalse(isInArea(0, -11, 3));
    }
}