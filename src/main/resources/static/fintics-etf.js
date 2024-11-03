function _createLineChart(elementId) {
    return new Chart(document.getElementById(elementId), {
        type: 'line',
        data: {
            datasets: []
        },
        options: {
            animation: false,
            maintainAspectRatio: false,
            parsing: {
                xAxisKey: 'dateTime',
                yAxisKey: 'pctChange'
            },
            scales: {
                x: {
                    type: 'time',
                    distribution: 'series',
                    time: {
                        unit: 'day',
                        displayFormats: {
                            day: 'MM-dd'
                        }
                    },
                    ticks: {
                        stepSize: 3,
                        maxTicksLimit: 10,
                        font: {
                            size: 8
                        }
                    },
                    title: {
                        display: true,
                        text: 'Daily',
                        color: '#911',
                        font: {
                            weight: 'bold',
                            lineHeight: 1.2,
                        }
                    }
                },
                y: {
                    ticks: {
                        stepSize: 0.5,
                        font: {
                            size: 8
                        },
                        callback: function(value, index, values) {
                            return value.toFixed(2) + ' %';
                        }
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.dataset.label + ': ' + context.raw['close'].toLocaleString() +
                                ' (' + context.raw['pctChange'] + '%)';
                        }
                    }
                }
            }
        }
    });
}