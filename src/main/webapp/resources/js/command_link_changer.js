function changeY(ylink, value) {
    document.getElementById('form:y').value = value;
    let links = document.getElementById('y-links').getElementsByClassName('y-checked');
    for (let i = 0; i < links.length; i++) {
        links[i].classList.remove('y-checked');
    }
    ylink.classList.add('y-checked');
}