function lookupCity() {
    const zipInput = document.getElementById('zip').value;
    const cityDisplay = document.getElementById('cityName');

    if (zipCodes[zipInput]) {
        cityDisplay.value = zipCodes[zipInput];
    } else {
        cityDisplay.value = '';
    }
}
