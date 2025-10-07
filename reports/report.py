from __future__ import print_function
import datetime
import json
import re
import sys
import org.HdrHistogram.Histogram as Histogram
import os
from collections import OrderedDict
import java.io.PrintStream as PrintStream
import java.io.ByteArrayOutputStream as ByteArrayOutputStream

# /// jbang
# requires-jython = "2.7.4"
# requires-java = "21"
# dependencies = [
#   "com.google.code.gson:gson:2.13.1",
#   "org.hdrhistogram:HdrHistogram:2.2.2",
#   "io.github.wfouche.tulip:tulip-runtime:2.1.12-dev"
# ]
# ///

summary_html_1 = '''<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style type="text/css">
    div.histo {
         display: none
    }
</style>

<!--Load the AJAX API-->
<script type="text/javascript" src="https://www.google.com/jsapi"></script>

<script type="text/javascript">

    if (window.File && window.FileReader && window.FileList && window.Blob) {
        // Great success! All the File APIs are supported.
    } else {
        alert('The File APIs are not fully supported in this browser.');
    }

    // Load the Visualization API and the corechart package.
    google.load('visualization', '1.0', {'packages':['corechart']});

    // Set a callback to run when the Google Visualization API is loaded.
    google.setOnLoadCallback(drawInitialChart);

    var chartData = null;
    var chart = null;

    function setChartData(names, histos) {
        while (names.length < histos.length) {
            names.push('Unknown');
        }

        var series = [];
        for (var i = 0; i < histos.length; i++) {
            series = appendDataSeries(histos[i], names[i], series);
        }

        chartData = google.visualization.arrayToDataTable(series);
    }

'''

text_block_1 = '''
    function drawInitialChart() {
        // Connect the choose files button:
        document.getElementById('files').addEventListener('change', handleFileSelect, false);

        // Load some static example data:
        var data1Str = document.querySelector("div#data_1").innerHTML.trim();
        var data2Str = document.querySelector("div#data_2").innerHTML.trim();
        var data3Str = document.querySelector("div#data_3").innerHTML.trim();
        var histos = [data3Str, data2Str, data1Str];
        var names = ['A', 'B', 'C'];

        setChartData(names, histos);
        drawChart();
    }
'''

summary_html_2 = '''
    var maxPercentile = 1000000;

    function drawChart() {

        var ticks =
                [{v:1,f:'0%'},
                    {v:10,f:'90%'},
                    {v:100,f:'99%'},
                    {v:1000,f:'99.9%'},
                    {v:10000,f:'99.99%'},
                    {v:100000,f:'99.999%'},
                    {v:1000000,f:'99.9999%'},
                    {v:10000000,f:'99.99999%'},
                    {v:100000000,f:'99.999999%'}];

        var unitSelection = document.getElementById("timeUnitSelection");
        var unitSelIndex = unitSelection.selectedIndex;
        var unitText = unitSelection.options[unitSelIndex].innerHTML;

        var options = {
//            title: 'Percentile Response Time Distribution',
            height: 480,
//            hAxis: {title: 'Percentile', minValue: 0, logScale: true, ticks:ticks },
            hAxis: {
                title: "Percentile",
                minValue: 1, logScale: true, ticks:ticks,
                viewWindowMode:'explicit',
                viewWindow:{
                    max:maxPercentile,
                    min:1
                }
            },
            vAxis: {title: 'Latency (' + unitText + ')', minValue: 0 },
            legend: {position: 'bottom'}
        };


        // add tooltips with correct percentile text to data:
        var columns = [0];
        for (var i = 1; i < chartData.getNumberOfColumns(); i++) {
            columns.push(i);
            columns.push({
                type: 'string',
                properties: {
                    role: 'tooltip'
                },
                calc: (function (j) {
                    return function (dt, row) {
                        var percentile = 100.0 - (100.0/dt.getValue(row, 0));
                        return dt.getColumnLabel(j) + ': ' +
                                percentile.toPrecision(7) +
                                '\%\\'ile = ' + dt.getValue(row, j) + ' ' + unitText
                    }
                })(i)
            });
        }
        var view = new google.visualization.DataView(chartData);
        view.setColumns(columns);

        chart = new google.visualization.LineChart(document.getElementById('chart_div'));
        chart.draw(view, options);

        google.visualization.events.addListener(chart, 'ready', function () {
            chart_div.innerHTML = '<img src="' + chart.getImageURI() + '">';
        });

    }
</script>
<script type="text/javascript">
    function appendDataSeries(histo, name, dataSeries) {
        var series;
        var seriesCount;
        if (dataSeries.length == 0) {
            series = [ ['X', name] ];
            seriesCount = 1;
        } else {
            series = dataSeries;
            series[0].push(name);
            seriesCount = series[0].length - 1;
        }

        var lines = histo.split("\\n");

        var seriesIndex = 1;
        for (var i = 0; i < lines.length; i++) {
            var line = lines[i].trim();
            var values = line.trim().split(/[ ]+/);

            if (line[0] != '#' && values.length == 4) {

                var y = parseFloat(values[0]);
                var x = parseFloat(values[3]);

                if (!isNaN(x) && !isNaN(y)) {

                    if (seriesIndex >= series.length) {
                        series.push([x]);
                    }

                    while (series[seriesIndex].length < seriesCount) {
                        series[seriesIndex].push(null);
                    }

                    series[seriesIndex].push(y);
                    seriesIndex++;
                }
            }
        }

        while (seriesIndex < series.length) {
            series[seriesIndex].push(null);
            seriesIndex++;
        }

        return series;
    }
</script>
<script>
    function timeUnitsSelected(evt) {
        drawChart();
        return {typed: ''};
    }

    function doExport(event) {
        saveSvgAsPng(document.querySelector('svg'), 'Histogram', 2.0);
        return {typed: ''};
    }
</script>

<script>
    function handleFileSelect(evt) {
        var files = evt.target.files; // FileList object
        var fileDisplayArea = document.getElementById('fileDisplayArea');

        var names = [];
        var histos = [];

        fileDisplayArea.innerText = "file selected...\\n";

        // Loop through the FileList and render image files as thumbnails.
        for (var i = 0, f; f = files[i]; i++) {
            var reader = new FileReader();

            reader.onload = (function(theFile) {
                return function(e) {
                    histos.push(e.target.result);
                    names.push(escape(theFile.name));
                    fileDisplayArea.innerText = " Plotting input from: " + names + "\\n";
                    setChartData(names, histos);
                    drawChart();
                };
            })(f);

            // Read in the image file as a data URL.
            reader.readAsText(f);
        }

    }

</script>

<script type="text/javascript">
    (function() {
        var out$ = typeof exports != 'undefined' && exports || this;

        var doctype = '<?xml version="1.0" standalone="no"?><!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">';

        function inlineImages(callback) {
            var images = document.querySelectorAll('svg image');
            var left = images.length;
            if (left == 0) {
                callback();
            }
            for (var i = 0; i < images.length; i++) {
                (function(image) {
                    if (image.getAttribute('xlink:href')) {
                        var href = image.getAttribute('xlink:href').value;
                        if (/^http/.test(href) && !(new RegExp('^' + window.location.host).test(href))) {
                            throw new Error("Cannot render embedded images linking to external hosts.");
                        }
                    }
                    var canvas = document.createElement('canvas');
                    var ctx = canvas.getContext('2d');
                    var img = new Image();
                    img.src = image.getAttribute('xlink:href');
                    img.onload = function() {
                        canvas.width = img.width;
                        canvas.height = img.height;
                        ctx.drawImage(img, 0, 0);
                        image.setAttribute('xlink:href', canvas.toDataURL('image/png'));
                        left--;
                        if (left == 0) {
                            callback();
                        }
                    }
                })(images[i]);
            }
        }

        function styles(dom) {
            var css = "";
            var sheets = document.styleSheets;
            for (var i = 0; i < sheets.length; i++) {
                if (sheets[i].hasOwnProperty('cssRules')) {
                    var rules = sheets[i].cssRules;
                    for (var j = 0; j < rules.length; j++) {
                        var rule = rules[j];
                        if (typeof(rule.style) != "undefined") {
                            css += rule.selectorText + " { " + rule.style.cssText + " }\\n";
                        }
                    }
                }
            }

            var s = document.createElement('style');
            s.setAttribute('type', 'text/css');
            s.innerHTML = "<![CDATA[\\n" + css + "\\n]]>";

            var defs = document.createElement('defs');
            defs.appendChild(s);
            return defs;
        }

        out$.svgAsDataUri = function(el, scaleFactor, cb) {
            scaleFactor = scaleFactor || 1;

            inlineImages(function() {
                var outer = document.createElement("div");
                var clone = el.cloneNode(true);
                var width = parseInt(
                        clone.getAttribute('width')
                        || clone.style.width
                        || out$.getComputedStyle(el).getPropertyValue('width')
                );
                var height = parseInt(
                        clone.getAttribute('height')
                        || clone.style.height
                        || out$.getComputedStyle(el).getPropertyValue('height')
                );

                var xmlns = "http://www.w3.org/2000/xmlns/";

                clone.setAttribute("version", "1.1");
                clone.setAttributeNS(xmlns, "xmlns", "http://www.w3.org/2000/svg");
                clone.setAttributeNS(xmlns, "xmlns:xlink", "http://www.w3.org/1999/xlink");
                clone.setAttribute("width", width * scaleFactor);
                clone.setAttribute("height", height * scaleFactor);
                clone.setAttribute("viewBox", "0 0 " + width + " " + height);
                outer.appendChild(clone);

                clone.insertBefore(styles(clone), clone.firstChild);

                var svg = doctype + outer.innerHTML;
                var uri = 'data:image/svg+xml;base64,' + window.btoa(unescape(encodeURIComponent(svg)));
                if (cb) {
                    cb(uri);
                }
            });
        }

        out$.saveSvgAsPng = function(el, name, scaleFactor) {
            out$.svgAsDataUri(el, scaleFactor, function(uri) {
                var image = new Image();
                image.src = uri;
                image.onload = function() {
                    var canvas = document.createElement('canvas');
                    canvas.width = image.width;
                    canvas.height = image.height;
                    var context = canvas.getContext('2d');
                    context.drawImage(image, 0, 0);

                    var a = document.createElement('a');
                    a.download = name;
                    a.href = canvas.toDataURL('image/png');
                    document.body.appendChild(a);
                    a.click();
                }
            });
        }
    })();
</script>

<style>
    .slider-width500
    {
        width: 500px;
    }
</style>

</head>

<body>

__CHARTS_TEXT__

<h2>Percentile Response Time Distribution</h2>

<input type="file" id="files" name="files[]" multiple />

<pre id="fileDisplayArea">Please select file(s) above.</pre>

<!--Div that will hold the chart-->
<div id="chart_div">None Loaded</div>

Latency time units:
<select name="units" size="1" id="timeUnitSelection" onChange="timeUnitsSelected()">
    <option value="Latency (seconds)">seconds</option>
    <option selected value="Latency (milliseconds)">milliseconds</option>
    <option value="Latency (qs)">microseconds</option>
    <option value="Latency (nanoseconds)">nanoseconds</option>
</select>
<button type='button' onclick='doExport(event)'>Export Image</button>

&nbsp; &nbsp; &nbsp; &nbsp;
<p>
Percentile range:

<input type="range" class="slider-width500"
       min="1" max="8" value="7" step="1"
       width="300px"
       onchange="showValue(this.value)" />
<span id="percentileRange">99.99999%</span>
<script type="text/javascript">
    function showValue(newValue) {
        var x = Math.pow(10, newValue);
        var percentile = 100.0 - (100.0 / x);
        document.getElementById("percentileRange").innerHTML=percentile + "%";
        maxPercentile = x;
        drawChart();
        return {typed: ''};
    }
</script>
</p>
'''

summary_html_table_1 = '''
<style>
table, th, td {
  border:1px solid black; font-size:16px; text-align: center;
}
mark {
  background-color: LightGray;
  color: black;
}
</style>

<h2>Percentile Response Time Distribution</h2>
'''

summary_html_3 = '''</body>
</html>
'''

# <h2><a href="https://wfouche.github.io/Tulip-docs">__DESC1__</a> / __DESC2__</h2>
# <h2>__DESC1__ / __DESC2__</h2>

header = '''<!DOCTYPE html>
<html lang="en">

<style>
table, th, td {
  border:1px solid black; font-size:16px; text-align: center;
}
th:nth-child(n+14) {
    background-color: #E5E4E2;
}
td:nth-child(n+14) {
    background-color: #E5E4E2;
}
</style>

<body>

<h2><a href="https://wfouche.github.io/Tulip-docs">__DESC1__</a> / __DESC2__</h2>

<table style="width:100%">
  <tr>
    <th>Benchmark</th>
    <th>Run Id</th>
    <th>#N</th>
    <th>#F</th>
    <th>Duration</th>
    <th>Aps</th>
    <th>Avg_Rt</th>
    <th>Std_Dev</th>
    <th>Min_Rt</th>
    <th>p90_Rt</th>
    <th>p99_Rt</th>
    <th>Max_Rt</th>
    <th>Max_Rtt</th>
    <th>AQS</th>
    <th>MQS</th>
    <th>AWT</th>
    <th>MWT</th>
    <th>CPU_T</th>
    <th>CPU</th>
    <th>MEM</th>
  </tr>
'''

benchmark_columns = '''
  <tr>
    <th>Benchmark</th>
    <th>Run Id</th>
    <th>#N</th>
    <th>#F</th>
    <th>Duration</th>
    <th>Aps</th>
    <th>Avg_Rt</th>
    <th>Std_Dev</th>
    <th>Min_Rt</th>
    <th>p90_Rt</th>
    <th>p99_Rt</th>
    <th>Max_Rt</th>
    <th>Max_Rtt</th>
    <th>AQS</th>
    <th>MQS</th>
    <th>AWT</th>
    <th>MWT</th>
    <th>CPU_T</th>
    <th>CPU</th>
    <th>MEM</th>
  </tr>
'''

benchmark_header = '''
  <tr>
    <td>%s</td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
  </tr>
'''

benchmark_empty_row = '''
  <tr>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
'''

benchmark_detail_row = '''
  <tr>
    <td>%s</td>
    <td>%d</td>
    <td>%d</td>
    <td>%d</td>
    <td>%s</td>
    <td>%.3f</td>
    <td>%.3f ms</td>
    <td>%.1f ms</td>
    <td>%.1f ms</td>
    <td>%.1f ms</td>
    <td>%.1f ms</td>
    <td>%.1f ms</td>
    <td>%s</td>
    <td>%.1f</td>
    <td>%d</td>
    <td>%.1f</td>
    <td>%.1f</td>
    <td>%s</td>
    <td>%.1f</td>
    <td>%.1f</td>
  </tr>
'''

benchmark_summary_row = '''
  <tr>
    <td>%s</td>
    <td>%s</td>
    <td><b>%d</b></td>
    <td><b><tag1>%d</tag1></b></td>
    <td><b>%s</b></td>
    <td><b><tag2>%.3f</tag2></b></td>
    <td><b>%.3f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%s</b></td>
    <td><b>%.1f</b></td>
    <td><b>%d</b></td>
    <td><b>%.1f</b></td>
    <td><b>%.1f</b></td>
    <td><b>%s</b></td>
    <td><b>%.1f</b></td>
    <td><b>%.1f</b></td>
  </tr>
'''

trailer = '''
</table>
</body>
</html>
'''

charts_html = '''
<h2>Actions per Second</h2>
  <div id="main" style="width: 100%;height:400px;"></div>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/echarts/6.0.0/echarts.min.js"></script>
  <script src="__JS_T_CHART__"></script>
<h2>Response Times (ms)</h2>
  <div id="main_p" style="width: 100%;height:400px;"></div>
  <script src="__JS_P_CHART__"></script>
'''

chart_t_html = '''
var myChart1 = echarts.init(document.getElementById('main'));

var data =  __DATA__;

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
myChart1.setOption(option);
'''

chart_p_html = '''
var myChart2 = echarts.init(document.getElementById('main_p'));

var data =  __DATA__;

var option = {
  legend: {},
  tooltip: {
    trigger: 'axis',
  },
  dataset: {
    source:data,
    dimensions: ['timestamp', 'sensor1', 'sensor2', 'sensor3', 'sensor4', 'sensor5', 'sensor6'],
  },
  xAxis: { type: 'time' },
  yAxis: { },
  series: [
  {
     name: 'Max',
     type: 'line',
     smooth: true,
     encode: {
       x: 'timestamp',
       y: 'sensor6' // refer sensor 1 value
     }
  },{
     name: 'p99',
     type: 'line',
     smooth: true,
     encode: {
       x: 'timestamp',
       y: 'sensor5' // refer sensor 1 value
     }
  },{
     name: 'p95',
     type: 'line',
     smooth: true,
     encode: {
       x: 'timestamp',
       y: 'sensor4' // refer sensor 1 value
     }

  },{
     name: 'p90',
     type: 'line',
     smooth: true,
     encode: {
       x: 'timestamp',
       y: 'sensor3' // refer sensor 1 value
     }

  },{
     name: 'Avg',
     type: 'line',
     smooth: true,
     encode: {
       x: 'timestamp',
       y: 'sensor2'
     }
  },{
     name: 'Min',
     type: 'line',
     smooth: true,
     encode: {
       x: 'timestamp',
       y: 'sensor1'
  }
}]
};
myChart2.setOption(option);

window.onresize = function() {
  myChart1.resize();
  myChart2.resize();
};
'''

class Summary:
    def __init__(self):
        self.num_actions = 0
        self.num_failed = 0
        self.duration = 0.0
        self.min_rt = 1000000000.0
        self.max_rt = 0.0
        self.max_rt_ts = ""
        self.mem = 0.0
        self.cpu = 0.0
        self.max_awt = 0.0
        self.max_wt = 0.0
        self.avg_qs = 0.0
        self.max_qs = 0
        self.name = ""
        self.cpu_time_ns = 0
        self.chart_t_list = []   # ['2018-04-10T20:40:33.100', 1100, 0]
        self.chart_p_list = []   # ['2018-04-10T20:40:33Z', 1, 5, 10, 20, 25, 30]
        self.aps_count = 0.0
        self.aps_target_sum = 0.0

def createReport(filename, text):

    cwd = os.getcwd()
    if os.path.isdir("build/reports/tulip"):
        os.chdir("build/reports/tulip")

    print("\nOutput filename = " + filename)

    if text[0] == '{':
        config_filename = ""
        config_filename2 = ""
    else:
        config_filename = text
        config_filename2 = os.path.basename(config_filename)

    jhh = {}
    jss = {}

    print_detail_rows = True

    global name2s
    global name2s_list
    global benchmark_id
    name2s = ""
    name2s_list = []
    benchmark_id = 0

    sm = None
    jh = Histogram(1, 3600*1000*1000, 3)
    fileObj = open(filename)
    jb = json.load(fileObj, object_pairs_hook=OrderedDict)
    version = jb['version']
    desc1 = 'Tulip ' + version
    rb = jb["results"]

    report_html_fn = jb["config"]["actions"]["report_filename"]
    report_html_fh = open(report_html_fn, "w+")

    report_json_fn = os.path.splitext(report_html_fn)[0] + ".json"
    report_json_fh = open(report_json_fn, "w+")
    report_json_fh.write("{\n")
    report_json_fh.write('  "config": ' + json.dumps(jb["config"]) + ',\n')
    report_json_fh.write('  "benchmarks": {\n')

    global report_dn
    report_dn = os.path.splitext(report_html_fn)[0]
    try:
        os.makedirs(report_dn)
    except:
        pass

    def odir(filename):
        global report_dn
        return os.path.join(os.getcwd(), report_dn, filename)

    def odirHtml(filename):
        global report_dn
        return report_dn + "/" + filename

    def name_to_href(name):
        href = '_' + re.sub(r'\W', '_', name.lower())
        hfile = os.path.splitext(config_filename2)[0] + ".html"
        return "<a href='%s#%s'>"%(hfile, href) + name + "</a>"

    if len(config_filename2) > 0:
        desc2 = "<a href='%s'>"%(os.path.splitext(config_filename2)[0] + ".html")
    else:
        desc2 = ""

    if len(config_filename2) > 0:
        desc2 += jb["config"]["actions"]["description"] + "</a> / " + jb["timestamp"][:-3]
    else:
        desc2 += jb["config"]["actions"]["description"] + " / " + jb["timestamp"][:-3]

    print("Report filename = " + report_html_fn)

    def printf(s):
        report_html_fh.write(s)

    def actionName(s):
        return s.split(",")[0].strip()

    def actionDesc(s):
        try:
            return s.split(",")[1].strip()
        except:
            return ""

    def str_from_cpu_time_ns(v_ns):
        r = str(datetime.timedelta(seconds=v_ns/1000000000.0))  #[:-5]
        if '.' in r:
            r = r[:-5]
        return r

    def print_global_summary():
        global name2s
        global name2s_list
        global benchmark_id

        avg_aps = 0.0 if sm.name in ["onStart", "onStop"] else sm.num_actions/sm.duration
        if sm.name in ["onStart", "onStop"]:
            cpu_t = "0:00:00"
            sm.cpu = 0.0
        else:
            cpu_t = str_from_cpu_time_ns(sm.cpu_time_ns)

        statsFilename = '%s_%d.html'%(odir(report_html_fn.split('.')[0]),benchmark_id)
        statsFilenameHtml = '%s_%d.html'%(odirHtml(report_html_fn.split('.')[0]),benchmark_id)
        text = "<a href='%s'>%s</a>"%(statsFilenameHtml,"[Summary]")
        printStream = PrintStream(statsFilename)
        printStream.print(summary_html_1)
        printStream.println()
        printStream.print('    function drawInitialChart() {')
        printStream.println()
        printStream.print('        // Connect the choose files button:')
        printStream.println()
        printStream.print("        document.getElementById('files').addEventListener('change', handleFileSelect, false);")
        printStream.println()
        printStream.println()
        printStream.print('        // Load some static example data:')
        printStream.println()
        idx = 0
        actionsString = ""
        histosString = ""
        for key in jss.keys():
            idx += 1
            smx = jss[key]
            jhx = jhh[key]
            if len(actionsString) > 0:
                actionsString += ', '
                histosString += ', '
            actionsString += "'A%s'"%(key)
            histosString += "data%dStr"%(idx)
            printStream.print('    var data%dStr = document.querySelector("div#data_%d").innerHTML.trim();'%(idx,idx))
            printStream.println()
        if idx > 1:
            actionsString += ', ' + "'S'"
            histosString += ', ' + "data%dStr"%(0)
            printStream.print('    var data%dStr = document.querySelector("div#data_%d").innerHTML.trim();'%(0,0))
            printStream.println()
        printStream.print('    var histos = [%s];'%(histosString))
        printStream.println()
        printStream.print('    var names = [%s];'%(actionsString))
        printStream.println()

        printStream.print('    setChartData(names, histos);')
        printStream.println()
        printStream.print('    drawChart();')
        printStream.println()
        printStream.print('    }')
        printStream.println()
        tChartFilename = '%s_%d_t.js'%(odir(report_html_fn.split('.')[0]),benchmark_id)
        tChartFilenameHtml = '%s_%d_t.js'%(odirHtml(report_html_fn.split('.')[0]),benchmark_id)
        pChartFilename = '%s_%d_p.js'%(odir(report_html_fn.split('.')[0]),benchmark_id)
        pChartFilenameHtml = '%s_%d_p.js'%(odirHtml(report_html_fn.split('.')[0]),benchmark_id)
        printStream.print(
            summary_html_2
                .replace("__CHARTS_TEXT__",
                    charts_html
                         .replace("__JS_T_CHART__",os.path.basename(tChartFilenameHtml))
                         .replace("__JS_P_CHART__",os.path.basename(pChartFilenameHtml)))
                )
        printStream.println()

        idx = 0
        for key in jss.keys():
            idx += 1
            smx = jss[key]
            jhx = jhh[key]
            printStream.print('<div id="data_%d" class="histo">'%(idx))
            printStream.println()
            jhx.outputPercentileDistribution(printStream, 1000.0)
            printStream.print('</div>')
            printStream.println()

        printStream.print('<div id="data_0" class="histo">')
        printStream.println()
        jh.outputPercentileDistribution(printStream, 1000.0)
        printStream.print('</div>')
        printStream.println()

        printStream.print(summary_html_table_1)
        print_percentile_table(printStream,jh)

        printStream.println()
        printStream.flush()
        printStream.close()

        # Actions/s chart
        #chart_t_list = [
        #    ['2018-04-10T20:40:33.100', 1100, 0],
        #    ['2018-04-10T20:40:53.200', 1002, 0],
        #    ['2018-04-10T20:41:03.300', 1104, 0],
        #    ['2018-04-10T20:44:03.400', 1205.5, 0],
        #    ['2018-04-10T20:45:03.500', 1306, 0]
        #]
        chart_p_list = [
            ['2018-04-10T20:40:33Z', 1, 5, 10, 20, 25, 30 ],
            ['2018-04-10T20:40:53Z', 2, 6, 15, 25, 30, 35],
            ['2018-04-10T20:41:03Z', 2, 5, 20, 30, 35, 40],
            ['2018-04-10T20:44:03Z', 3, 7, 12, 22, 30, 32],
            ['2018-04-10T20:45:03Z', 2, 10, 13, 23, 30, 33]
        ]
        printStream = PrintStream(tChartFilename)
        printStream.print(chart_t_html.replace("__DATA__",("%s"%(sm.chart_t_list)).replace("u'","'")))
        printStream.println()
        printStream.flush()
        printStream.close()

        # Response Time (ms) chart
        printStream = PrintStream(pChartFilename)
        printStream.print(chart_p_html.replace("__DATA__",("%s"%(sm.chart_p_list)).replace("u'","'")))
        printStream.println()
        printStream.flush()
        printStream.close()

        if True:
            rd = {}
            rd["num_actions"] = sm.num_actions
            rd["num_failed"] = sm.num_failed
            rd["duration"] = str(datetime.timedelta(seconds=int(sm.duration)))
            rd["aps"] = avg_aps
            rd["aps_target_rate"] = sm.aps_target_sum / sm.aps_count
            rd["avg_rt"] = jh.getMean()/1000.0
            rd["std_dev"] = jh.getStdDeviation()/1000.0
            rd["min_rt"] = sm.min_rt
            rd["p90_rt"] = jh.getValueAtPercentile(90.0)/1000.0
            rd["p99_rt"] = jh.getValueAtPercentile(99.0)/1000.0
            rd["max_rt"] = sm.max_rt
            rd["max_rtt"] = sm.max_rt_ts.replace("_","T")
            rd["AQS"] = sm.avg_qs
            rd["MQS"] = sm.max_qs
            rd["AWT"] = sm.max_awt
            rd["MWT"] = sm.max_wt
            rd["CPU_T"] = cpu_t
            rd["CPU"] = sm.cpu
            rd["MEM"] = sm.mem

            report_json_fh.write('        ,"summary": %s\n'%(json.dumps(rd)))

        html = benchmark_summary_row%(
            name2s,
            text,
            sm.num_actions,
            sm.num_failed,
            str(datetime.timedelta(seconds=int(sm.duration))),
            avg_aps,
            jh.getMean()/1000.0,
            jh.getStdDeviation()/1000.0,
            sm.min_rt,
            jh.getValueAtPercentile(90.0)/1000.0,
            jh.getValueAtPercentile(99.0)/1000.0,
            sm.max_rt,
            sm.max_rt_ts[8:],
            sm.avg_qs,
            sm.max_qs,
            sm.max_awt,
            sm.max_wt,
            cpu_t,
            sm.cpu,
            sm.mem)
        if not print_detail_rows:
            html = html.replace("<b>","")
            html = html.replace("</b>","")
        # Validation: #F
        if sm.num_failed > 0:
            html = html.replace("<tag1>","<mark>")
            html = html.replace("</tag1>","</mark>")
        else:
            html = html.replace("<tag1>","")
            html = html.replace("</tag1>","")
        # Validation: Avg_APS
        #print(sm.name)
        target_aps = rd["aps_target_rate"]
        if target_aps > 0.0:
            delta_percentage_aps = 100.0*abs(target_aps-avg_aps)/target_aps
            if delta_percentage_aps > 1.5:
                html = html.replace("<tag2>","<mark>")
                html = html.replace("</tag2>","</mark>")
            else:
                html = html.replace("<tag2>","")
                html = html.replace("</tag2>","")
        else:
            html = html.replace("<tag2>","")
            html = html.replace("</tag2>","")
        printf(html)
        if len(name2s_list) > 0:
            name2s = name2s_list[0]
            del name2s_list[0]

    def print_percentile_table(printStream, jhx):
        bos = ByteArrayOutputStream()
        ox = PrintStream(bos)
        jhx.outputPercentileDistribution(ox, 1000.0)
        ox.flush()
        ox.close()
        ptext = bos.toString()
        lines = ptext.split('\n')
        tcount = jhx.getTotalCount()
        header = True
        rlist = []
        for line in lines:
            if len(line) == 0:
                continue
            if line[0] != "#":
                e = line.split()
                if header:
                    printStream.println('<table style="width:600px">')
                    printStream.println('  <tr>')
                    printStream.println('    <th>%s</th>'%(e[0]))
                    printStream.println('    <th>%s</th>'%(e[1]))
                    printStream.println('    <th>%s</th>'%(e[2]))
                    printStream.println('    <th>%s</th>'%(e[3]))
                    printStream.println('    <th>AboveCount</th>')
                    printStream.println('  </tr>')
                    header = False
                else:
                    if len(e) == 3:
                        e.append('')
                    rlist.append(e)
        rlist.reverse()
        for e in rlist:
            mark = False
            if e[1].startswith("0.9990"):
                mark = True
            if e[1].startswith("0.990"):
                mark = True
            if e[1] == "0.950000000000":
                mark = True
            if e[1] == "0.900000000000":
                mark = True
            if e[1] == "0.800000000000":
                mark = True
            if e[1] == "0.500000000000":
                mark = True
            printStream.println('  <tr>')
            if mark:
                printStream.println('    <td><mark>%s</mark></td>'%(e[0]))
                printStream.println('    <td><mark>%s</mark></td>'%(e[1]))
            else:
                printStream.println('    <td>%s</td>'%(e[0]))
                printStream.println('    <td>%s</td>'%(e[1]))
            printStream.println('    <td>%s</td>'%(e[2]))
            printStream.println('    <td>%s</td>'%(e[3]))
            printStream.println('    <td>%s</td>'%(tcount-int(e[2])))
            printStream.println('  </tr>')
        printStream.println("</table>")

    def print_action_summary():
        global name2s
        global name2s_list
        global benchmark_id
        page_id = 0
        num_keys = len(jss.keys())
        for key in jss.keys():
            smx = jss[key]
            jhx = jhh[key]
            if jb["config"]["actions"]["user_actions"].has_key(key):
                text = "[%s.%s]"%(key, actionName(jb["config"]["actions"]["user_actions"][key]))
            else:
                text = "[%s]"%(key)
            statsFilename = '%s_%d_%d.html'%(odir(report_html_fn.split('.')[0]),benchmark_id,page_id)
            statsFilenameHtml = '%s_%d_%d.html'%(odirHtml(report_html_fn.split('.')[0]),benchmark_id,page_id)
            statsFilenamePrev = '%s_%d_%d.html'%(report_html_fn.split('.')[0],benchmark_id,abs(page_id-1))
            statsFilenameNext = '%s_%d_%d.html'%(report_html_fn.split('.')[0],benchmark_id,page_id+1)
            text = "<a href='%s'>%s</a>"%(statsFilenameHtml,text)
            printStream = PrintStream(statsFilename)
            printStream.println("<html lang=\"en\">")
            printStream.println("<style>")
            printStream.println("table, th, td {")
            printStream.println("  border:1px solid black; font-size:16px; text-align: center;")
            printStream.println("}")
            printStream.println("mark {")
            printStream.println("  background-color: LightGray;")
            printStream.println("  color: black;")
            printStream.println("}")
            printStream.println("</style>")
            printStream.println("<body>")
            printStream.println('<a href="../%s">Up</a>'%(report_html_fn))
            if (page_id == 0) and (num_keys == 1):
                printStream.println('<a href="#">Prev</a>')
                printStream.println('<a href="#">Next</a>')
            elif (page_id == 0) and (num_keys > 1):
                printStream.println('<a href="#">Prev</a>')
                printStream.println('<a href="%s">Next</a>'%(statsFilenameNext))
            elif page_id+1 == num_keys:
                printStream.println('<a href="%s">Prev</a>'%(statsFilenamePrev))
                printStream.println('<a href="#">Next</a>')
            else:
                printStream.println('<a href="%s">Prev</a>'%(statsFilenamePrev))
                printStream.println('<a href="%s">Next</a>'%(statsFilenameNext))

            if jb["config"]["actions"]["user_actions"].has_key(key):
                desc = "[%s.%s]"%(key, actionName(jb["config"]["actions"]["user_actions"][key]))
            else:
                desc = "[A%s]"%(key)
            printStream.println("<h2>%s Percentile Response Time Distribution</h2>"%(desc))

            #printStream.println("<pre>")
            #jhx.outputPercentileDistribution(printStream, 1000.0)

            print_percentile_table(printStream,jhx)

            #printStream.println("</pre>")

            printStream.println("</body>")
            printStream.println("</html>")
            printStream.flush()
            printStream.close()
            avg_aps = 0.0 if smx.name in ["onStart", "onStop"] else smx.num_actions/smx.duration
            if smx.name in ["onStart", "onStop"]:
                cpu_t = "0:00:00"
                smx.cpu = 0.0
            else:
                cpu_t = str_from_cpu_time_ns(smx.cpu_time_ns)

            if True:
                rd = {}
                rd["num_actions"] = smx.num_actions
                rd["num_failed"] = smx.num_failed
                rd["duration"] = str(datetime.timedelta(seconds=int(sm.duration)))
                rd["aps"] = avg_aps
                rd["avg_rt"] = jhx.getMean()/1000.0
                rd["std_dev"] = jhx.getStdDeviation()/1000.0
                rd["min_rt"] = smx.min_rt
                rd["p90_rt"] = jhx.getValueAtPercentile(90.0)/1000.0
                rd["p99_rt"] = jhx.getValueAtPercentile(99.0)/1000.0
                rd["max_rt"] = smx.max_rt
                rd["max_rtt"] = smx.max_rt_ts.replace("_","T")
                if page_id == 0:
                    report_json_fh.write('      }\n')
                    report_json_fh.write('      ,"actions": {\n')
                    report_json_fh.write('         "%s": %s\n'%(key,json.dumps(rd)))
                else:
                    report_json_fh.write('        ,"%s": %s\n'%(key,json.dumps(rd)))

            html = benchmark_summary_row%(name2s,text,smx.num_actions,smx.num_failed,str(datetime.timedelta(seconds=int(sm.duration))),avg_aps,jhx.getMean()/1000.0,jhx.getStdDeviation()/1000.0,smx.min_rt,jhx.getValueAtPercentile(90.0)/1000.0,jhx.getValueAtPercentile(99.0)/1000.0,smx.max_rt,smx.max_rt_ts[8:],smx.avg_qs,smx.max_qs,smx.max_awt,smx.max_wt,cpu_t,smx.cpu,smx.mem)
            if not print_detail_rows:
                html = html.replace("<b>","")
                html = html.replace("</b>","")
            # Remove tag1
            html = html.replace("<tag1>","")
            html = html.replace("</tag1>","")
            # Remove tag2
            html = html.replace("<tag2>","")
            html = html.replace("</tag2>","")

            printf(html)
            if len(name2s_list) > 0:
                name2s = name2s_list[0]
                del name2s_list[0]

            page_id += 1
        #report_json_fh.write('      }\n')

    printf(header.replace("__DESC1__", desc1).replace("__DESC2__", desc2))
    json_bm_names = {}
    prev_row_id = 0
    for e in rb:
        current_row_id = int(e["row_id"])
        if current_row_id <= prev_row_id:
            if sm is not None:
                print_action_summary()
                print_global_summary()
                printf(benchmark_empty_row)
                printf(benchmark_columns)
            sm = Summary()
            sm.name = e["bm_name"]
            jh.reset()
            benchmark_id += 1
            jhh = {}
            jss = {}
            printf(benchmark_header%(name_to_href(e["bm_name"])))
            if len(e["workflow_name"]) > 0:
                name2s_list = ["u:%d, t:%d"%(e["num_users"],e["num_threads"]), "w:%s"%(e["workflow_name"]), "c:%d"%(e["context_id"]) ,""]
            else:
                name2s_list = ["u:%d, t:%d"%(e["num_users"],e["num_threads"]), "c:%d"%(e["context_id"]),""]
            name2s = name2s_list[0]
            del name2s_list[0]
        ht = Histogram.fromString(e["histogram_rt"])
        jh.add(ht)
        p_mem = 100.0 * e["jvm_memory_used"] / e["jvm_memory_maximum"]
        p_cpu = e["process_cpu_utilization"]
        if e["bm_name"] in ["onStart", "onStop"]:
            cpu_t = "0:00:00"
            p_cpu = 0.0
        else:
            cpu_t = str_from_cpu_time_ns(e["process_cpu_time_ns"])
            p_cpu = e["process_cpu_utilization"]
        if not json_bm_names.has_key(e["bm_name"]):
            json_bm_names[e["bm_name"]] = 1
            if len(json_bm_names.keys()) > 1:
                report_json_fh.write('      }\n')
                report_json_fh.write('    },\n')
            report_json_fh.write('    "%s": {\n'%(e["bm_name"]))
            context = {}
            context["name"] = e["context_name"]
            context["num_users"] = e["num_users"]
            context["num_threads"] = e["num_threads"]
            report_json_fh.write('      "context": %s,'%(json.dumps(context)))
            report_json_fh.write('      "results": {\n')

        if True:
            rd = {}
            rd["num_actions"] = e["num_actions"]
            rd["num_failed"] = e["num_failed"]
            rd["duration"] = str(datetime.timedelta(seconds=int(e["duration"])))
            rd["aps"] = 0.0 if e["bm_name"] in ["onStart", "onStop"] else e["avg_aps"]
            rd["aps_target_rate"] = e["aps_target_rate"]
            rd["avg_rt"] = e["avg_rt"]
            rd["std_dev"] = ht.getStdDeviation()/1000.0
            rd["min_rt"] = e["min_rt"]
            rd["p90_rt"] = e["percentiles_rt"]["90.0"]
            rd["p99_rt"] = e["percentiles_rt"]["99.0"]
            rd["max_rt"] = e["max_rt"]
            rd["max_rtt"] = e["max_rt_ts"].replace("_","T")
            rd["AQS"] = e["avg_wthread_qsize"]
            rd["MQS"] = e["max_wthread_qsize"]
            rd["AWT"] = e["avg_wt"]
            rd["MWT"] = e["max_wt"]
            rd["CPU_T"] = cpu_t
            rd["CPU"] = p_cpu
            rd["MEM"] = p_mem
            if e["row_id"]+1 == 1:
                report_json_fh.write('         "%d": %s\n'%(e["row_id"]+1,json.dumps(rd)))
            else:
                report_json_fh.write('        ,"%d": %s\n'%(e["row_id"]+1,json.dumps(rd)))
        if print_detail_rows:
            printf(benchmark_detail_row%( \
                name2s,
                e["row_id"]+1,
                e["num_actions"],
                e["num_failed"],
                str(datetime.timedelta(seconds=int(e["duration"]))),
                0.0 if e["bm_name"] in ["onStart", "onStop"] else e["avg_aps"],
                e["avg_rt"],
                ht.getStdDeviation()/1000.0,
                e["min_rt"],
                e["percentiles_rt"]["90.0"],
                e["percentiles_rt"]["99.0"],
                e["max_rt"],
                e["max_rt_ts"][8:],
                e["avg_wthread_qsize"],
                e["max_wthread_qsize"],
                e["avg_wt"],
                e["max_wt"],
                cpu_t,
                p_cpu,
                p_mem
                ))
            if len(name2s_list) > 0:
                name2s = name2s_list[0]
                del name2s_list[0]
        if sm.min_rt > e["min_rt"]:
            sm.min_rt = e["min_rt"]
        if sm.max_rt < e["max_rt"]:
            sm.max_rt = e["max_rt"]
            sm.max_rt_ts = e["max_rt_ts"]
        sm.num_actions += e["num_actions"]
        sm.num_failed += e["num_failed"]
        sm.duration += e["duration"]
        if sm.avg_qs < e["avg_wthread_qsize"]:
            sm.avg_qs = e["avg_wthread_qsize"]
        if sm.max_qs < e["max_wthread_qsize"]:
            sm.max_qs = e["max_wthread_qsize"]
        if sm.max_awt < e["avg_wt"]:
            sm.max_awt = e["avg_wt"]
        if sm.max_wt < e["max_wt"]:
            sm.max_wt = e["max_wt"]
        if sm.mem < p_mem:
            sm.mem = p_mem
        if sm.cpu < p_cpu:
            sm.cpu = p_cpu
        sm.cpu_time_ns += e["process_cpu_time_ns"]
        sm.aps_count += 1.0
        sm.aps_target_sum += e["aps_target_rate"]

        if len(sm.chart_t_list) == -1:
            sm.chart_t_list.append(
            [
                '%s'%(e["test_begin"].replace("_", "T")),
                float("%.1f"%(e["avg_aps"])),
                float("%.3f"%(e["num_failed"]/e["duration"]))
            ])

        sm.chart_t_list.append(
            [
                '%s'%(e["test_end"].replace("_", "T")),
                float("%.1f"%(e["avg_aps"])),
                float("%.3f"%(e["num_failed"]/e["duration"]))
             ])

        # ['2018-04-10T20:40:33Z', 1, 5, 10, 20, 25, 30 ]
        if len(sm.chart_p_list) == -1:
            sm.chart_p_list.append(
                [
                    '%s'%(e["test_begin"].replace("_", "T")),
                    float("%.1f"%(e["min_rt"])),
                    float("%.1f"%(e["avg_rt"])),
                    float("%.1f"%(ht.getValueAtPercentile(90.0)/1000.0)),
                    float("%.1f"%(ht.getValueAtPercentile(95.0)/1000.0)),
                    float("%.1f"%(ht.getValueAtPercentile(99.0)/1000.0)),
                    float("%.1f"%(e["max_rt"]))
                ])

        sm.chart_p_list.append(
            [
                '%s'%(e["test_end"].replace("_", "T")),
                float("%.1f"%(e["min_rt"])),
                float("%.1f"%(e["avg_rt"])),
                float("%.1f"%(ht.getValueAtPercentile(90.0)/1000.0)),
                float("%.1f"%(ht.getValueAtPercentile(95.0)/1000.0)),
                float("%.1f"%(ht.getValueAtPercentile(99.0)/1000.0)),
                float("%.1f"%(e["max_rt"]))
            ])

        # jhh ...
        for key in e["user_actions"].keys():
            ar = e["user_actions"][key]
            htt = Histogram.fromString(ar["histogram_rt"])
            #print(ar["name"] + " - " + "%.3f"%(htt.getMean()/1000.0))
            if jhh.has_key(key):
                jhh[key].add(htt)
            else:
                jhh[key] = Histogram(1, 3600*1000*1000, 3)
                jhh[key].add(htt)
            #print(ar["name"] + " - " + "%.3f"%(jhh[key].getMean()/1000.0) + " - %d"%(jhh[key].getTotalCount()))

        # jss ...
        for key in e["user_actions"].keys():
            ar = e["user_actions"][key]
            if jss.has_key(key):
                smx = jss[key]
            else:
                smx = jss[key] = Summary()
                smx.name = e["bm_name"]

            if smx.min_rt > ar["min_rt"]:
                smx.min_rt = ar["min_rt"]
            if smx.max_rt < ar["max_rt"]:
                smx.max_rt = ar["max_rt"]
                smx.max_rt_ts = ar["max_rt_ts"]
            smx.num_actions += ar["num_actions"]
            smx.num_failed += ar["num_failed"]

            smx.duration += e["duration"]
            if smx.avg_qs < e["avg_wthread_qsize"]:
                smx.avg_qs = e["avg_wthread_qsize"]
            if smx.max_qs < e["max_wthread_qsize"]:
                smx.max_qs = e["max_wthread_qsize"]
            if smx.max_awt < e["avg_wt"]:
                smx.max_awt = e["avg_wt"]
            if smx.max_wt < e["max_wt"]:
                smx.max_wt = e["max_wt"]
            if smx.mem < p_mem:
                smx.mem = p_mem
            if smx.cpu < p_cpu:
                smx.cpu = p_cpu
            smx.cpu_time_ns += e["process_cpu_time_ns"]

    print_action_summary()
    print_global_summary()
    printf(trailer)

    report_html_fh.close()

    report_json_fh.write("      }\n")
    report_json_fh.write("    }\n")
    report_json_fh.write("  }\n")
    report_json_fh.write("}\n")
    report_json_fh.close()

    os.chdir(cwd)

if __name__ == "__main__":
    if len(sys.argv) >= 3:
        filename = sys.argv[1]
        configFilename = sys.argv[2]
        createReport(filename, configFilename)
