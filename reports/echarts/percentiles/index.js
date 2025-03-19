var myChart = echarts.init(document.getElementById('main'));

var data =  [
      ['2018-04-10T20:40:33Z', 1, 5],
      ['2018-04-10T20:40:53Z', 2, 3],
      ['2018-04-10T20:41:03Z', 4, 2],
      ['2018-04-10T20:44:03Z', 5, 1],
      ['2018-04-10T20:45:03Z', 6, 0]
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
     name: '90p',
     type: 'line',
     encode: {
       x: 'timestamp',
       y: 'sensor1' // refer sensor 1 value 
     }
  },{
     name: 'Max',
     type: 'line',
     encode: {
       x: 'timestamp',
       y: 'sensor2'
  }
}]
};
myChart.setOption(option);
