package io.github.wfouche.tulip.report

import org.python.util.PythonInterpreter

val jythonCode: String = """
from __future__ import print_function
import datetime
import json
import sys
import org.HdrHistogram.Histogram as Histogram
import os
from collections import OrderedDict
import java.io.PrintStream as PrintStream

summary_html_1 = '''<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<style type="text/css">
    div.histo {
        visibility: hidden
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
            title: 'Latency by Percentile Distribution',
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
        var out${'$'} = typeof exports != 'undefined' && exports || this;

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

        out${'$'}.svgAsDataUri = function(el, scaleFactor, cb) {
            scaleFactor = scaleFactor || 1;

            inlineImages(function() {
                var outer = document.createElement("div");
                var clone = el.cloneNode(true);
                var width = parseInt(
                        clone.getAttribute('width')
                        || clone.style.width
                        || out${'$'}.getComputedStyle(el).getPropertyValue('width')
                );
                var height = parseInt(
                        clone.getAttribute('height')
                        || clone.style.height
                        || out${'$'}.getComputedStyle(el).getPropertyValue('height')
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

        out${'$'}.saveSvgAsPng = function(el, name, scaleFactor) {
            out${'$'}.svgAsDataUri(el, scaleFactor, function(uri) {
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
<h2>Benchmark: __BENCHMARK_NAME__</h2>

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
<p>
    <br>
*** Note: Input files are expected to be in the .hgrm format produced by
HistogramLogProcessor, or the percentile output format for HdrHistogram.
See example file format
    <a href="https://github.com/HdrHistogram/HdrHistogram/blob/master/GoogleChartsExample/example1.txt">here</a>
</p>
<!--<h4>Expected Service Level:</h4>-->
<!--<input type="checkbox" name="ESL" value="ESL">Plot Expected Service Level<br>-->
<!--Percentile:-->
<!--<input type="text" id="ESLPercentile0" name="ESLPercentile0" size="6" value = 90 />-->
<!--% &nbsp &nbsp &nbsp Limit:-->
<!--<input type="text" id="ESLLimit0" name="ESLLimit0" size="12"/>-->
<!--<br>-->
<!--Percentile:-->
<!--<input type="text" id="ESLPercentile1" name="ESLPercentile1" size="6" value = 99 />-->
<!--% &nbsp &nbsp &nbsp Limit:-->
<!--<input type="text" id="ESLLimit1" name="ESLLimit1" size="12"/>-->
<!--<br>-->
<!--Percentile:-->
<!--<input type="text" id="ESLPercentile2" name="ESLPercentile2" size="6" value = 99.99 />-->
<!--% &nbsp &nbsp &nbsp Limit:-->
<!--<input type="text" id="ESLLimit2" name="ESLLimit2" size="12"/>-->
<!--<br>-->
<!--Percentile:-->
<!--<input type="text" id="ESLPercentile3" name="ESLPercentile2" size="6" value="100.0" readonly/>-->
<!--% &nbsp &nbsp &nbsp Limit:-->
<!--<input type="text" id="ESLLimit3" name="ESLLimit2" size="12"/>-->

'''

summary_html_3 = '''</body>
</html>
'''

# <h2><a href="https://wfouche.github.io/Tulip-docs">__DESC1__</a> / __DESC2__</h2>
# <h2>__DESC1__ / __DESC2__</h2>

header = '''<!DOCTYPE html>
<html>

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
    <th>Stdev</th>
    <th>Min_Rt</th>
    <th>90p_Rt</th>
    <th>99p_Rt</th>
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
    <th>Stdev</th>
    <th>Min_Rt</th>
    <th>90p_Rt</th>
    <th>99p_Rt</th>
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

class Summary:
    num_actions = 0
    num_failed = 0
    duration = 0.0
    min_rt = 1000000000.0
    max_rt = 0.0
    max_rt_ts = ""
    mem = 0.0
    cpu = 0.0
    max_awt = 0.0
    max_wt = 0.0
    avg_qs = 0.0
    max_qs = 0
    name = ""
    cpu_time_ns = 0

def createReport(filename, text):

    print("\nOutput filename = " + filename)

    if text[0] == '{':
        config_filename = ""
    else:
        config_filename = text

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

    report_fn = jb["config"]["actions"]["report_filename"]
    report_fh = open(report_fn, "w+")

    if len(config_filename) > 0:
        desc2 = "<a href='%s'>"%(os.path.splitext(config_filename)[0] + ".adoc")
    else:
        desc2 = ""

    if len(config_filename) > 0:
        desc2 += jb["config"]["actions"]["description"] + "</a> / " + jb["timestamp"][:-3]
    else:
        desc2 += jb["config"]["actions"]["description"] + " / " + jb["timestamp"][:-3]

    print("Report filename = " + report_fn)

    def printf(s):
        report_fh.write(s)

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

        statsFilename = '%s_%d.html'%(report_fn.split('.')[0],benchmark_id)
        text = "<a href='%s'>%s</a>"%(statsFilename,"Charts")
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
        printStream.print(summary_html_2.replace("__BENCHMARK_NAME__", sm.name))
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

        printStream.print(summary_html_3)
        printStream.println()
        printStream.flush()
        printStream.close()

        html = benchmark_summary_row%(name2s,text,sm.num_actions,sm.num_failed,str(datetime.timedelta(seconds=int(sm.duration))),avg_aps,jh.getMean()/1000.0,jh.getStdDeviation()/1000.0,sm.min_rt,jh.getValueAtPercentile(90.0)/1000.0,jh.getValueAtPercentile(99.0)/1000.0,sm.max_rt,sm.max_rt_ts[8:],sm.avg_qs,sm.max_qs,sm.max_awt,sm.max_wt,cpu_t,sm.cpu,sm.mem)
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
        if "aps_rate" in jb["config"]["benchmarks"][sm.name].keys():
            target_aps = jb["config"]["benchmarks"][sm.name]["aps_rate"]
        else:
            target_aps = 0.0
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

    def print_action_summary():
        global name2s
        global name2s_list
        global benchmark_id
        for key in jss.keys():
            smx = jss[key]
            jhx = jhh[key]
            if jb["config"]["actions"]["user_actions"].has_key(key):
                text = "[%s.%s]"%(key, jb["config"]["actions"]["user_actions"][key])
            else:
                text = "[%s]"%(key)
            statsFilename = '%s_%d_%d.html'%(report_fn.split('.')[0],benchmark_id,int(key))
            text = "<a href='%s'>%s</a>"%(statsFilename,text)
            printStream = PrintStream(statsFilename)
            printStream.print("<html>")
            printStream.println()
            printStream.print("<body>")
            printStream.println()
            printStream.print("<h2>Name:  %s, Action Id: %s</h2>"%(smx.name,key))
            printStream.println()
            printStream.print("<h3>Latency by Percentile Distribution</h3>")
            printStream.println()
            printStream.print("<pre>")
            printStream.println()
            jhx.outputPercentileDistribution(printStream, 1000.0)
            printStream.print("</pre>")
            printStream.println()
            printStream.print("</body>")
            printStream.println()
            printStream.print("</html>")
            printStream.println()
            printStream.flush()
            printStream.close()
            avg_aps = 0.0 if smx.name in ["onStart", "onStop"] else smx.num_actions/smx.duration
            if smx.name in ["onStart", "onStop"]:
                cpu_t = "0:00:00"
                smx.cpu = 0.0
            else:
                cpu_t = str_from_cpu_time_ns(smx.cpu_time_ns)
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

    printf(header.replace("__DESC1__", desc1).replace("__DESC2__", desc2))

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
            printf(benchmark_header%(e["bm_name"]))
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

    report_fh.close()
"""

fun createHtmlReport(outputFilename: String, text1: String) {
    //System.out.println("---")
    //System.out.println("text: " + text1)
    //System.out.println("---")

    var text2: String = text1.trim()
    if (text2.startsWith("{")) {
        text2 = "{}"
    }

    PythonInterpreter().use { pyInterp ->
        pyInterp.exec(jythonCode)
        pyInterp.eval("createReport(\"${outputFilename}\",\"${text2}\")")
    }
}

val jythonCode2: String = """
from __future__ import print_function
import json
import sys
import com.google.gson.JsonParser as JsonParser
import os
from collections import OrderedDict

header = '''= __DESCRIPTION__
:toc: left
:sectnums:
:diagram-server-url: https://kroki.io/
:diagram-server-type: kroki_io

Filename::
  __CONFIG_FILENAME__

== Actions

[%header,cols="1a,2a"]
|===
| id | value
'''

def createReport(filename):

    def printf(s):
        report_fh.write(s)

    def generate_table(e):
        printf("| *" + e + "*\n")
        printf("|\n")
        printf('[%header,cols="1a,2a"]\n')
        printf('!===\n')
        printf('! id ! value \n')
        for k in jb['actions'][e].keys():
            printf('! *' + k + '* ')
            printf('! ' + str(jb['actions'][e][k]) + '\n')
        printf('!===\n')

    def generate_workflow():
        diagId = -1
        def name_to_id(s):
            if s in '-':
                return 0
            return int(s)
        def action_name(s):
            if s in jb['actions']['user_actions'].keys():
                return jb['actions']['user_actions'][s]
            return '<unknown>'

        #print(jb.keys())
        #print(jb["workflows"].keys())
        for wn in jb["workflows"].keys():
            diagId += 1
            printf("\n")
            printf("[[%s]]\n"%(wn))
            printf("=== " + "%s\n"%(wn))
            printf("\n")
            printf('[%header,cols="1a,1a"]\n')
            printf('|===\n')
            printf('| Workflow Diagram | Specification\n')
            printf('|')
            printf("[plantuml,wfd%d,svg]"%(diagId) + '\n')
            printf('----\n')
            printf('@startuml\n')
            #printf('title %s\n'%(wn))
            for sname in jb['workflows'][wn].keys():
                if sname in ['-']:
                    printf('state "-" as A0\n')
                    continue
                sid = int(sname)
                printf('state "Action %d" as A%d\n'%(sid,sid))
                printf('A%d: <%s>\n'%(sid,action_name(sname)))
                printf('\n')
            for sname in jb['workflows'][wn].keys():
                if sname in ['*']:
                    continue
                if sname in ['-']:
                    mid = 0
                else:
                    mid = int(sname)
                for k in jb['workflows'][wn][sname].keys():
                    nid = name_to_id(k)
                    fv = jb['workflows'][wn][sname][k]
                    printf('A%d --> A%d: %.3f\n'%(mid,nid,fv))
            printf('@enduml\n')
            printf('----\n')
            printf('| \n')
            printf('[source,json]\n')
            printf('----\n')
            printf('%s\n'%(json.dumps(jb['workflows'][wn], indent=4)))
            printf('----\n')
            printf('|===\n')

    #print("\nConfig filename = " + filename)

    # .jsonc -> .adoc
    f_ext = os.path.splitext(filename)[1]
    report_fn = filename[:-len(f_ext)]+".adoc"
    report_fh = open(report_fn, "w+")

    # Remove all JSONC comments from the JSON
    sf = open(filename,'r').read()
    gsonJsonTree = JsonParser.parseString(sf)
    jsonWithoutComments = gsonJsonTree.toString()
    gsonJsonTree = None

    # Restore JSON from String
    jb = json.loads(jsonWithoutComments, object_pairs_hook=OrderedDict)
    jsonWithoutComments = None

    # print header
    printf(header.replace("__CONFIG_FILENAME__", filename).replace("__DESCRIPTION__", jb['actions']['description']))

    # Actions
    for e in jb['actions'].keys():
        if e in ['user_params', 'user_actions']:
            generate_table(e)
            continue
        # | *description*
        # | Micro-benchmarks
        printf("| *" + e + '*\n')
        printf("| " + jb['actions'][e] + '\n')
    printf("|===" + '\n')

    # Workflows
    if "workflows" in jb.keys():
        printf("\n")
        printf("== Workflows \n")
        generate_workflow()

    # Benchmarks Data
    printf("\n")
    printf("== Benchmarks\n")
    for k in jb['benchmarks'].keys():
        b = jb['benchmarks'][k]
        printf("\n")
        printf('=== %s'%(k) + '\n')
        printf("\n")
        printf('[%header,cols="1a,2a"]\n')
        printf("|===\n")
        printf("| id | value\n")
        # enabled
        if "enabled" in b.keys():
            printf('| *enabled* | %s\n'%(b["enabled"]))
        else:
            printf('| *enabled* | True\n')
        # aps_rate
        if "aps_rate" in b.keys():
            printf('| *aps_rate* | %.1f\n'%(b["aps_rate"]))
        else:
            printf('| *aps_rate* | 0.0\n')
        # worker_thread_queue_size
        if "worker_thread_queue_size" in b.keys():
            printf('| *worker_thread_queue_size* | %d\n'%(b["worker_thread_queue_size"]))
        else:
            printf('| *worker_thread_queue_size* | 0\n')
        # workflow
        if "scenario_workflow" in b.keys():
            workflow_is_defined = b["scenario_workflow"] in jb["workflows"].keys()
            if workflow_is_defined:
                printf('| *scenario_workflow* | <<%s>>\n'%(b["scenario_workflow"]))
            else:
                printf('| *scenario_workflow* | Error, scenario_workflow *%s* is not defined\n'%(b["scenario_workflow"]))
        elif "scenario_actions" in b.keys():
            printf('| *scenario_actions* \n')
            printf('| \n')
            printf('[%header,cols="1a,2a"]\n')
            printf('!===\n')
            printf('! id ! weight \n')
            for a in b["scenario_actions"]:
                printf('! %d\n'%(a["id"]))
                if "weight" in a.keys():
                    printf('! %d \n'%(a["weight"]))
                else:
                    printf('! - \n')
            printf('!===\n')
        # time
        if "time" in b.keys():
            printf('| *time* \n')
            printf('| \n')
            printf('[%noheader,cols="2a,1a"]\n')
            printf('!===\n')
            #printf('! id ! value \n')
            for k in b["time"].keys():
                printf('! *%s*\n'%(k))
                printf('! %d seconds\n'%(b["time"][k]))
            printf('!===\n')

        printf("|===\n")

    # Context Data
    printf("\n")
    printf("== Contexts\n")
    printf("\n")

    for k in jb['contexts'].keys():
        c = jb['contexts'][k]
        printf('=== %s'%(k) + '\n')
        printf('\n')
        printf('[%header,cols="1a,2a"]\n')
        printf('|===\n')
        printf('| id | value \n')
        if "enabled" in c.keys():
            printf('| *enabled* | %s\n'%(c["enabled"]))
        else:
            printf('| *enabled* | True\n')
        printf('| *num_users*   | %d\n'%(c["num_users"]))
        printf('| *num_threads* | %d\n'%(c["num_threads"]))
        printf("|===\n")

    report_fh.close()
"""

fun createConfigReport(configFilename: String) {
    PythonInterpreter().use { pyInterp ->
        pyInterp.exec(jythonCode2)
        pyInterp.eval("createReport(\"${configFilename}\")")
    }
}