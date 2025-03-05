from __future__ import print_function
import datetime
import json
import sys
import org.HdrHistogram.Histogram as Histogram
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
<h2>Summary: __BENCHMARK_NAME__</h2>

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

<div id="data_1" class="histo">
    Value   Percentile   TotalCount 1/(1-Percentile)
    0.016     0.000000            1         1.00
    0.980     0.100000        47530         1.11
    3.609     0.200000        95042         1.25
    6.783     0.300000       142539         1.43
    9.183     0.400000       190154         1.67
    11.167     0.500000       237581         2.00
    12.295     0.550000       261367         2.22
    13.759     0.600000       285105         2.50
    15.375     0.650000       308930         2.86
    16.703     0.700000       332844         3.33
    18.639     0.750000       356366         4.00
    19.951     0.775000       368356         4.44
    21.951     0.800000       380158         5.00
    24.559     0.825000       391991         5.71
    28.095     0.850000       403848         6.67
    32.591     0.875000       415744         8.00
    35.743     0.887500       421673         8.89
    40.671     0.900000       427627        10.00
    47.423     0.912500       433573        11.43
    56.895     0.925000       439503        13.33
    80.639     0.937500       445415        16.00
    97.023     0.943750       448385        17.78
    127.231     0.950000       451374        20.00
    133.887     0.956250       454341        22.86
    138.623     0.962500       457329        26.67
    143.231     0.968750       460283        32.00
    145.919     0.971875       461775        35.56
    149.247     0.975000       463239        40.00
    153.855     0.978125       464725        45.71
    159.103     0.981250       466207        53.33
    165.247     0.984375       467687        64.00
    171.647     0.985938       468439        71.11
    179.967     0.987500       469178        80.00
    190.591     0.989062       469916        91.43
    206.207     0.990625       470658       106.67
    231.551     0.992188       471407       128.00
    252.799     0.992969       471771       142.22
    257.023     0.993750       472143       160.00
    261.759     0.994531       472547       182.86
    263.423     0.995313       472887       213.33
    269.055     0.996094       473274       256.00
    272.383     0.996484       473441       284.44
    289.023     0.996875       473627       320.00
    311.551     0.997266       473819       365.71
    367.359     0.997656       474009       426.67
    540.159     0.998047       474186       512.00
    713.727     0.998242       474274       568.89
    999.935     0.998437       474367       640.00
    1164.287     0.998633       474461       731.43
    1229.823     0.998828       474554       853.33
    1260.543     0.999023       474647      1024.00
    1273.855     0.999121       474693      1137.78
    1284.095     0.999219       474743      1280.00
    1291.263     0.999316       474785      1462.86
    1302.527     0.999414       474836      1706.67
    1318.911     0.999512       474878      2048.00
    1332.223     0.999561       474901      2275.56
    1373.183     0.999609       474924      2560.00
    1420.287     0.999658       474947      2925.71
    1707.007     0.999707       474970      3413.33
    3104.767     0.999756       474994      4096.00
    3653.631     0.999780       475005      4551.11
    5152.767     0.999805       475017      5120.00
    5660.671     0.999829       475030      5851.43
    5967.871     0.999854       475040      6826.67
    6840.319     0.999878       475052      8192.00
    6873.087     0.999890       475067      9102.22
    6873.087     0.999902       475067     10240.00
    7028.735     0.999915       475069     11702.86
    7237.631     0.999927       475075     13653.33
    7663.615     0.999939       475081     16384.00
    7700.479     0.999945       475083     18204.44
    7991.295     0.999951       475086     20480.00
    8200.191     0.999957       475090     23405.71
    8273.919     0.999963       475092     27306.67
    8445.951     0.999969       475096     32768.00
    8445.951     0.999973       475096     36408.89
    8454.143     0.999976       475102     40960.00
    8454.143     0.999979       475102     46811.43
    8454.143     0.999982       475102     54613.33
    8454.143     0.999985       475102     65536.00
    8470.527     0.999986       475103     72817.78
    9068.543     0.999988       475106     81920.00
    9068.543     0.999989       475106     93622.86
    9068.543     0.999991       475106    109226.67
    9068.543     0.999992       475106    131072.00
    9068.543     0.999993       475106    145635.56
    9199.615     0.999994       475107    163840.00
    9199.615     0.999995       475107    187245.71
    9199.615     0.999995       475107    218453.33
    9420.799     0.999996       475108    262144.00
    9420.799     0.999997       475108    291271.11
    9420.799     0.999997       475108    327680.00
    9420.799     0.999997       475108    374491.43
    9420.799     0.999998       475108    436906.67
    9428.991     0.999998       475109    524288.00
    9428.991     1.000000       475109          inf
    #[Mean    =       25.048, StdDeviation   =      120.097]
    #[Max     =     9420.800, Total count    =       475109]
    #[Buckets =           27, SubBuckets     =         2048]
</div>
<div id='data_2' class='histo'>
    Value   Percentile   TotalCount 1/(1-Percentile)
    0.017     0.000000            7         1.00
    4.747     0.100000       110499         1.11
    10.791     0.200000       221161         1.25
    17.311     0.300000       331501         1.43
    136.319     0.400000       441939         1.67
    1418.239     0.500000       813215         2.00
    1418.239     0.550000       813215         2.22
    1418.239     0.600000       813215         2.50
    1418.239     0.650000       813215         2.86
    1418.239     0.700000       813215         3.33
    1477.631     0.750000       965043         4.00
    1477.631     0.775000       965043         4.44
    1477.631     0.800000       965043         5.00
    1477.631     0.825000       965043         5.71
    1477.631     0.850000       965043         6.67
    1510.399     0.875000      1068006         8.00
    1510.399     0.887500      1068006         8.89
    1510.399     0.900000      1068006        10.00
    1510.399     0.912500      1068006        11.43
    1510.399     0.925000      1068006        13.33
    1510.399     0.937500      1068006        16.00
    1510.399     0.943750      1068006        17.78
    1510.399     0.950000      1068006        20.00
    1510.399     0.956250      1068006        22.86
    1510.399     0.962500      1068006        26.67
    1511.423     0.968750      1104769        32.00
    1511.423     1.000000      1104769          inf
    #[Mean    =      856.989, StdDeviation   =      705.812]
    #[Max     =     1510.400, Total count    =      1104769]
    #[Buckets =           27, SubBuckets     =         2048]
</div>
<div id='data_3' class='histo'>
    Value   Percentile   TotalCount 1/(1-Percentile)
    0.016     0.000000            1         1.00
    4.455     0.100000       109166         1.11
    10.367     0.200000       218441         1.25
    14.383     0.300000       327522         1.43
    19.471     0.400000       436590         1.67
    30.447     0.500000       545740         2.00
    39.519     0.550000       600381         2.22
    52.191     0.600000       654888         2.50
    68.223     0.650000       709558         2.86
    87.807     0.700000       763927         3.33
    109.951     0.750000       818641         4.00
    121.791     0.775000       845803         4.44
    134.399     0.800000       873094         5.00
    150.911     0.825000       900473         5.71
    186.495     0.850000       927626         6.67
    258.303     0.875000       954920         8.00
    356.351     0.887500       968559         8.89
    505.087     0.900000       982215        10.00
    676.351     0.912500       995841        11.43
    863.743     0.925000      1009492        13.33
    1067.007     0.937500      1023145        16.00
    1181.695     0.943750      1029991        17.78
    1363.967     0.950000      1036758        20.00
    1889.279     0.956250      1043588        22.86
    2480.127     0.962500      1050404        26.67
    3108.863     0.968750      1057243        32.00
    3452.927     0.971875      1060633        35.56
    3821.567     0.975000      1064053        40.00
    4206.591     0.978125      1067484        45.71
    4591.615     0.981250      1070864        53.33
    4988.927     0.984375      1074290        64.00
    5193.727     0.985938      1075999        71.11
    5402.623     0.987500      1077691        80.00
    5623.807     0.989062      1079393        91.43
    5881.855     0.990625      1081093       106.67
    6164.479     0.992188      1082817       128.00
    6307.839     0.992969      1083659       142.22
    6455.295     0.993750      1084511       160.00
    6602.751     0.994531      1085360       182.86
    6758.399     0.995313      1086216       213.33
    6938.623     0.996094      1087066       256.00
    7057.407     0.996484      1087500       284.44
    7180.287     0.996875      1087921       320.00
    7315.455     0.997266      1088345       365.71
    7458.815     0.997656      1088775       426.67
    7614.463     0.998047      1089198       512.00
    7696.383     0.998242      1089413       568.89
    7782.399     0.998437      1089619       640.00
    7876.607     0.998633      1089835       731.43
    7970.815     0.998828      1090048       853.33
    8073.215     0.999023      1090260      1024.00
    8126.463     0.999121      1090366      1137.78
    8179.711     0.999219      1090473      1280.00
    8237.055     0.999316      1090583      1462.86
    8302.591     0.999414      1090691      1706.67
    8376.319     0.999512      1090794      2048.00
    8413.183     0.999561      1090847      2275.56
    8454.143     0.999609      1090904      2560.00
    8536.063     0.999658      1090951      2925.71
    8642.559     0.999707      1091009      3413.33
    8732.671     0.999756      1091061      4096.00
    8781.823     0.999780      1091086      4551.11
    8830.975     0.999805      1091115      5120.00
    8880.127     0.999829      1091140      5851.43
    8929.279     0.999854      1091169      6826.67
    8978.431     0.999878      1091194      8192.00
    9003.007     0.999890      1091210      9102.22
    9027.583     0.999902      1091222     10240.00
    9052.159     0.999915      1091235     11702.86
    9068.543     0.999927      1091246     13653.33
    9117.695     0.999939      1091259     16384.00
    9142.271     0.999945      1091266     18204.44
    9166.847     0.999951      1091273     20480.00
    9191.423     0.999957      1091279     23405.71
    9215.999     0.999963      1091285     27306.67
    9248.767     0.999969      1091291     32768.00
    9273.343     0.999973      1091295     36408.89
    9289.727     0.999976      1091298     40960.00
    9306.111     0.999979      1091302     46811.43
    9330.687     0.999982      1091306     54613.33
    9338.879     0.999985      1091308     65536.00
    9355.263     0.999986      1091311     72817.78
    9355.263     0.999988      1091311     81920.00
    9371.647     0.999989      1091314     93622.86
    9379.839     0.999991      1091315    109226.67
    9388.031     0.999992      1091317    131072.00
    9388.031     0.999993      1091317    145635.56
    9396.223     0.999994      1091318    163840.00
    9404.415     0.999995      1091320    187245.71
    9404.415     0.999995      1091320    218453.33
    9404.415     0.999996      1091320    262144.00
    9412.607     0.999997      1091321    291271.11
    9412.607     0.999997      1091321    327680.00
    9420.799     0.999997      1091323    374491.43
    9420.799     0.999998      1091323    436906.67
    9420.799     0.999998      1091323    524288.00
    9420.799     0.999998      1091323    582542.22
    9420.799     0.999998      1091323    655360.00
    9420.799     0.999999      1091323    748982.86
    9420.799     0.999999      1091323    873813.33
    9420.799     0.999999      1091323   1048576.00
    9428.991     0.999999      1091324   1165084.44
    9428.991     1.000000      1091324          inf
    #[Mean    =      297.175, StdDeviation   =      976.346]
    #[Max     =     9420.800, Total count    =      1091324]
    #[Buckets =           27, SubBuckets     =         2048]
</div>
</body>
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

def createReport(filename):

    print("\nOutput filename = " + filename)

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
    desc2 = jb["config"]["actions"]["description"] + " / " + jb["timestamp"]
    rb = jb["results"]

    report_fn = jb["config"]["actions"]["report_filename"]
    report_fh = open(report_fn, "w+")

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
        text = "<a href='%s'>%s</a>"%(statsFilename,"[Summary]")
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
        for key in jss.keys():
            idx += 1
            smx = jss[key]
            jhx = jhh[key]
            if len(actionsString) > 0:
                actionsString += ', '
            actionsString += "'A%s'"%(key)
            printStream.print('    var data%dStr = document.querySelector("div#data_%d").innerHTML.trim();'%(idx,idx))
            printStream.println()
        histosString = ""
        while idx > 0:
            if len(histosString) == 0:
                histosString = "data%dStr"%(idx)
            else:
                histosString += ", " + "data%dStr"%(idx)
            idx -= 1
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
            printStream.print("<h3>Response Time Percentile Distribution</h3>")
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

if __name__ == "__main__":
    filename = sys.argv[1]
    createReport(filename)
