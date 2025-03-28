var myChart = echarts.init(document.getElementById('main'));

var data =  [
      ['2018-04-10T20:40:33.100', 1100, 5],
      ['2018-04-10T20:40:53.200', 1002, 3],
      ['2018-04-10T20:41:03.300', 1104, 200],
      ['2018-04-10T20:44:03.400', 1205.5, 100],
      ['2018-04-10T20:45:03.500', 1306, 0.000001]
];

var option = {
  legend: {},
  tooltip: {
    trigger: 'axis',
  },
  dataset: {
    source:data,
    dimensions: ['timestamp', 'sensor1', 'sensor2'],
  },
  xAxis: { type: 'time' },
  yAxis: { },
  series: [
  {
     name: 'Actions/s',
     type: 'line',
     smooth: true,
     encode: {
       x: 'timestamp',
       y: 'sensor1' // refer sensor 1 value 
     }
  },{
     name: 'Failures/s',
     type: 'line',
     smooth: true,
     encode: {
       x: 'timestamp',
       y: 'sensor2'
  }
}]
};
myChart.setOption(option);
