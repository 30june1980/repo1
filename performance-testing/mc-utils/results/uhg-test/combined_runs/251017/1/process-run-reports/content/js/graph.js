/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
$(document).ready(function() {

    $(".click-title").mouseenter( function(    e){
        e.preventDefault();
        this.style.cursor="pointer";
    });
    $(".click-title").mousedown( function(event){
        event.preventDefault();
    });

    // Ugly code while this script is shared among several pages
    try{
        refreshHitsPerSecond(true);
    } catch(e){}
    try{
        refreshResponseTimeOverTime(true);
    } catch(e){}
    try{
        refreshResponseTimePercentiles();
    } catch(e){}
    $(".portlet-header").css("cursor", "auto");
});

var percentileThreshold = 0;
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

// Fixes time stamps
function fixTimeStamps(series, offset){
    $.each(series, function(index, item) {
        $.each(item.data, function(index, coord) {
            coord[0] += offset;
        });
    });
}

// Check if the specified jquery object is a graph
function isGraph(object){
    return object.data('plot') !== undefined;
}

/**
 * Export graph to a PNG
 */
function exportToPNG(graphName, target) {
    var plot = $("#"+graphName).data('plot');
    var flotCanvas = plot.getCanvas();
    var image = flotCanvas.toDataURL();
    image = image.replace("image/png", "image/octet-stream");
    
    var downloadAttrSupported = ("download" in document.createElement("a"));
    if(downloadAttrSupported === true) {
        target.download = graphName + ".png";
        target.href = image;
    }
    else {
        document.location.href = image;
    }
    
}

// Override the specified graph options to fit the requirements of an overview
function prepareOverviewOptions(graphOptions){
    var overviewOptions = {
        series: {
            shadowSize: 0,
            lines: {
                lineWidth: 1
            },
            points: {
                // Show points on overview only when linked graph does not show
                // lines
                show: getProperty('series.lines.show', graphOptions) == false,
                radius : 1
            }
        },
        xaxis: {
            ticks: 2,
            axisLabel: null
        },
        yaxis: {
            ticks: 2,
            axisLabel: null
        },
        legend: {
            show: false,
            container: null
        },
        grid: {
            hoverable: false
        },
        tooltip: false
    };
    return $.extend(true, {}, graphOptions, overviewOptions);
}

// Force axes boundaries using graph extra options
function prepareOptions(options, data) {
    options.canvas = true;
    var extraOptions = data.extraOptions;
    if(extraOptions !== undefined){
        var xOffset = options.xaxis.mode === "time" ? -18000000 : 0;
        var yOffset = options.yaxis.mode === "time" ? -18000000 : 0;

        if(!isNaN(extraOptions.minX))
        	options.xaxis.min = parseFloat(extraOptions.minX) + xOffset;
        
        if(!isNaN(extraOptions.maxX))
        	options.xaxis.max = parseFloat(extraOptions.maxX) + xOffset;
        
        if(!isNaN(extraOptions.minY))
        	options.yaxis.min = parseFloat(extraOptions.minY) + yOffset;
        
        if(!isNaN(extraOptions.maxY))
        	options.yaxis.max = parseFloat(extraOptions.maxY) + yOffset;
    }
}

// Filter, mark series and sort data
function prepareSeries(data){
    var result = data.result;

    // Keep only series when needed
    if(seriesFilter && (!filtersOnlySampleSeries || result.supportsControllersDiscrimination)){
        // Insensitive case matching
        var regexp = new RegExp(seriesFilter, 'i');
        result.series = $.grep(result.series, function(series, index){
            return regexp.test(series.label);
        });
    }

    // Keep only controllers series when supported and needed
    if(result.supportsControllersDiscrimination && showControllersOnly){
        result.series = $.grep(result.series, function(series, index){
            return series.isController;
        });
    }

    // Sort data and mark series
    $.each(result.series, function(index, series) {
        series.data.sort(compareByXCoordinate);
        series.color = index;
    });
}

// Set the zoom on the specified plot object
function zoomPlot(plot, xmin, xmax, ymin, ymax){
    var axes = plot.getAxes();
    // Override axes min and max options
    $.extend(true, axes, {
        xaxis: {
            options : { min: xmin, max: xmax }
        },
        yaxis: {
            options : { min: ymin, max: ymax }
        }
    });

    // Redraw the plot
    plot.setupGrid();
    plot.draw();
}

// Prepares DOM items to add zoom function on the specified graph
function setGraphZoomable(graphSelector, overviewSelector){
    var graph = $(graphSelector);
    var overview = $(overviewSelector);

    // Ignore mouse down event
    graph.bind("mousedown", function() { return false; });
    overview.bind("mousedown", function() { return false; });

    // Zoom on selection
    graph.bind("plotselected", function (event, ranges) {
        // clamp the zooming to prevent infinite zoom
        if (ranges.xaxis.to - ranges.xaxis.from < 0.00001) {
            ranges.xaxis.to = ranges.xaxis.from + 0.00001;
        }
        if (ranges.yaxis.to - ranges.yaxis.from < 0.00001) {
            ranges.yaxis.to = ranges.yaxis.from + 0.00001;
        }

        // Do the zooming
        var plot = graph.data('plot');
        zoomPlot(plot, ranges.xaxis.from, ranges.xaxis.to, ranges.yaxis.from, ranges.yaxis.to);
        plot.clearSelection();

        // Synchronize overview selection
        overview.data('plot').setSelection(ranges, true);
    });

    // Zoom linked graph on overview selection
    overview.bind("plotselected", function (event, ranges) {
        graph.data('plot').setSelection(ranges);
    });

    // Reset linked graph zoom when reseting overview selection
    overview.bind("plotunselected", function () {
        var overviewAxes = overview.data('plot').getAxes();
        zoomPlot(graph.data('plot'), overviewAxes.xaxis.min, overviewAxes.xaxis.max, overviewAxes.yaxis.min, overviewAxes.yaxis.max);
    });
}

var responseTimePercentilesInfos = {
        data: {"result": {"minY": 81.0, "minX": 0.0, "maxY": 3120.0, "series": [{"data": [[0.0, 81.0], [0.1, 86.0], [0.2, 87.0], [0.3, 87.0], [0.4, 87.0], [0.5, 88.0], [0.6, 88.0], [0.7, 88.0], [0.8, 88.0], [0.9, 88.0], [1.0, 89.0], [1.1, 89.0], [1.2, 89.0], [1.3, 89.0], [1.4, 89.0], [1.5, 89.0], [1.6, 89.0], [1.7, 89.0], [1.8, 89.0], [1.9, 90.0], [2.0, 90.0], [2.1, 90.0], [2.2, 90.0], [2.3, 90.0], [2.4, 90.0], [2.5, 90.0], [2.6, 90.0], [2.7, 90.0], [2.8, 90.0], [2.9, 90.0], [3.0, 90.0], [3.1, 90.0], [3.2, 91.0], [3.3, 91.0], [3.4, 91.0], [3.5, 91.0], [3.6, 91.0], [3.7, 91.0], [3.8, 91.0], [3.9, 91.0], [4.0, 91.0], [4.1, 91.0], [4.2, 91.0], [4.3, 91.0], [4.4, 91.0], [4.5, 91.0], [4.6, 91.0], [4.7, 91.0], [4.8, 91.0], [4.9, 91.0], [5.0, 91.0], [5.1, 91.0], [5.2, 91.0], [5.3, 92.0], [5.4, 92.0], [5.5, 92.0], [5.6, 92.0], [5.7, 92.0], [5.8, 92.0], [5.9, 92.0], [6.0, 92.0], [6.1, 92.0], [6.2, 92.0], [6.3, 92.0], [6.4, 92.0], [6.5, 92.0], [6.6, 92.0], [6.7, 92.0], [6.8, 92.0], [6.9, 92.0], [7.0, 92.0], [7.1, 92.0], [7.2, 92.0], [7.3, 92.0], [7.4, 92.0], [7.5, 92.0], [7.6, 92.0], [7.7, 92.0], [7.8, 93.0], [7.9, 93.0], [8.0, 93.0], [8.1, 93.0], [8.2, 93.0], [8.3, 93.0], [8.4, 93.0], [8.5, 93.0], [8.6, 93.0], [8.7, 93.0], [8.8, 93.0], [8.9, 93.0], [9.0, 93.0], [9.1, 93.0], [9.2, 93.0], [9.3, 93.0], [9.4, 93.0], [9.5, 93.0], [9.6, 93.0], [9.7, 93.0], [9.8, 93.0], [9.9, 93.0], [10.0, 93.0], [10.1, 93.0], [10.2, 93.0], [10.3, 93.0], [10.4, 93.0], [10.5, 93.0], [10.6, 93.0], [10.7, 93.0], [10.8, 93.0], [10.9, 93.0], [11.0, 93.0], [11.1, 94.0], [11.2, 94.0], [11.3, 94.0], [11.4, 94.0], [11.5, 94.0], [11.6, 94.0], [11.7, 94.0], [11.8, 94.0], [11.9, 94.0], [12.0, 94.0], [12.1, 94.0], [12.2, 94.0], [12.3, 94.0], [12.4, 94.0], [12.5, 94.0], [12.6, 94.0], [12.7, 94.0], [12.8, 94.0], [12.9, 94.0], [13.0, 94.0], [13.1, 94.0], [13.2, 94.0], [13.3, 94.0], [13.4, 94.0], [13.5, 94.0], [13.6, 94.0], [13.7, 94.0], [13.8, 94.0], [13.9, 94.0], [14.0, 94.0], [14.1, 94.0], [14.2, 94.0], [14.3, 94.0], [14.4, 94.0], [14.5, 94.0], [14.6, 94.0], [14.7, 94.0], [14.8, 94.0], [14.9, 95.0], [15.0, 95.0], [15.1, 95.0], [15.2, 95.0], [15.3, 95.0], [15.4, 95.0], [15.5, 95.0], [15.6, 95.0], [15.7, 95.0], [15.8, 95.0], [15.9, 95.0], [16.0, 95.0], [16.1, 95.0], [16.2, 95.0], [16.3, 95.0], [16.4, 95.0], [16.5, 95.0], [16.6, 95.0], [16.7, 95.0], [16.8, 95.0], [16.9, 95.0], [17.0, 95.0], [17.1, 95.0], [17.2, 95.0], [17.3, 95.0], [17.4, 95.0], [17.5, 95.0], [17.6, 95.0], [17.7, 95.0], [17.8, 95.0], [17.9, 95.0], [18.0, 95.0], [18.1, 95.0], [18.2, 95.0], [18.3, 95.0], [18.4, 95.0], [18.5, 95.0], [18.6, 95.0], [18.7, 95.0], [18.8, 95.0], [18.9, 95.0], [19.0, 96.0], [19.1, 96.0], [19.2, 96.0], [19.3, 96.0], [19.4, 96.0], [19.5, 96.0], [19.6, 96.0], [19.7, 96.0], [19.8, 96.0], [19.9, 96.0], [20.0, 96.0], [20.1, 96.0], [20.2, 96.0], [20.3, 96.0], [20.4, 96.0], [20.5, 96.0], [20.6, 96.0], [20.7, 96.0], [20.8, 96.0], [20.9, 96.0], [21.0, 96.0], [21.1, 96.0], [21.2, 96.0], [21.3, 96.0], [21.4, 96.0], [21.5, 96.0], [21.6, 96.0], [21.7, 96.0], [21.8, 96.0], [21.9, 96.0], [22.0, 96.0], [22.1, 96.0], [22.2, 96.0], [22.3, 96.0], [22.4, 96.0], [22.5, 96.0], [22.6, 96.0], [22.7, 96.0], [22.8, 96.0], [22.9, 96.0], [23.0, 96.0], [23.1, 96.0], [23.2, 97.0], [23.3, 97.0], [23.4, 97.0], [23.5, 97.0], [23.6, 97.0], [23.7, 97.0], [23.8, 97.0], [23.9, 97.0], [24.0, 97.0], [24.1, 97.0], [24.2, 97.0], [24.3, 97.0], [24.4, 97.0], [24.5, 97.0], [24.6, 97.0], [24.7, 97.0], [24.8, 97.0], [24.9, 97.0], [25.0, 97.0], [25.1, 97.0], [25.2, 97.0], [25.3, 97.0], [25.4, 97.0], [25.5, 97.0], [25.6, 97.0], [25.7, 97.0], [25.8, 97.0], [25.9, 97.0], [26.0, 97.0], [26.1, 97.0], [26.2, 97.0], [26.3, 97.0], [26.4, 97.0], [26.5, 97.0], [26.6, 97.0], [26.7, 97.0], [26.8, 97.0], [26.9, 97.0], [27.0, 97.0], [27.1, 97.0], [27.2, 97.0], [27.3, 97.0], [27.4, 97.0], [27.5, 98.0], [27.6, 98.0], [27.7, 98.0], [27.8, 98.0], [27.9, 98.0], [28.0, 98.0], [28.1, 98.0], [28.2, 98.0], [28.3, 98.0], [28.4, 98.0], [28.5, 98.0], [28.6, 98.0], [28.7, 98.0], [28.8, 98.0], [28.9, 98.0], [29.0, 98.0], [29.1, 98.0], [29.2, 98.0], [29.3, 98.0], [29.4, 98.0], [29.5, 98.0], [29.6, 98.0], [29.7, 98.0], [29.8, 98.0], [29.9, 98.0], [30.0, 98.0], [30.1, 98.0], [30.2, 98.0], [30.3, 98.0], [30.4, 98.0], [30.5, 98.0], [30.6, 98.0], [30.7, 98.0], [30.8, 98.0], [30.9, 98.0], [31.0, 98.0], [31.1, 98.0], [31.2, 98.0], [31.3, 98.0], [31.4, 98.0], [31.5, 98.0], [31.6, 98.0], [31.7, 98.0], [31.8, 98.0], [31.9, 99.0], [32.0, 99.0], [32.1, 99.0], [32.2, 99.0], [32.3, 99.0], [32.4, 99.0], [32.5, 99.0], [32.6, 99.0], [32.7, 99.0], [32.8, 99.0], [32.9, 99.0], [33.0, 99.0], [33.1, 99.0], [33.2, 99.0], [33.3, 99.0], [33.4, 99.0], [33.5, 99.0], [33.6, 99.0], [33.7, 99.0], [33.8, 99.0], [33.9, 99.0], [34.0, 99.0], [34.1, 99.0], [34.2, 99.0], [34.3, 99.0], [34.4, 99.0], [34.5, 99.0], [34.6, 99.0], [34.7, 99.0], [34.8, 99.0], [34.9, 99.0], [35.0, 99.0], [35.1, 99.0], [35.2, 99.0], [35.3, 99.0], [35.4, 99.0], [35.5, 99.0], [35.6, 99.0], [35.7, 99.0], [35.8, 99.0], [35.9, 99.0], [36.0, 99.0], [36.1, 100.0], [36.2, 100.0], [36.3, 100.0], [36.4, 100.0], [36.5, 100.0], [36.6, 100.0], [36.7, 100.0], [36.8, 100.0], [36.9, 100.0], [37.0, 100.0], [37.1, 100.0], [37.2, 100.0], [37.3, 100.0], [37.4, 100.0], [37.5, 100.0], [37.6, 100.0], [37.7, 100.0], [37.8, 100.0], [37.9, 100.0], [38.0, 100.0], [38.1, 100.0], [38.2, 100.0], [38.3, 100.0], [38.4, 100.0], [38.5, 100.0], [38.6, 100.0], [38.7, 100.0], [38.8, 100.0], [38.9, 100.0], [39.0, 100.0], [39.1, 100.0], [39.2, 100.0], [39.3, 100.0], [39.4, 100.0], [39.5, 100.0], [39.6, 100.0], [39.7, 100.0], [39.8, 100.0], [39.9, 100.0], [40.0, 100.0], [40.1, 100.0], [40.2, 101.0], [40.3, 101.0], [40.4, 101.0], [40.5, 101.0], [40.6, 101.0], [40.7, 101.0], [40.8, 101.0], [40.9, 101.0], [41.0, 101.0], [41.1, 101.0], [41.2, 101.0], [41.3, 101.0], [41.4, 101.0], [41.5, 101.0], [41.6, 101.0], [41.7, 101.0], [41.8, 101.0], [41.9, 101.0], [42.0, 101.0], [42.1, 101.0], [42.2, 101.0], [42.3, 101.0], [42.4, 101.0], [42.5, 101.0], [42.6, 101.0], [42.7, 101.0], [42.8, 101.0], [42.9, 101.0], [43.0, 101.0], [43.1, 101.0], [43.2, 101.0], [43.3, 101.0], [43.4, 101.0], [43.5, 101.0], [43.6, 101.0], [43.7, 101.0], [43.8, 101.0], [43.9, 101.0], [44.0, 102.0], [44.1, 102.0], [44.2, 102.0], [44.3, 102.0], [44.4, 102.0], [44.5, 102.0], [44.6, 102.0], [44.7, 102.0], [44.8, 102.0], [44.9, 102.0], [45.0, 102.0], [45.1, 102.0], [45.2, 102.0], [45.3, 102.0], [45.4, 102.0], [45.5, 102.0], [45.6, 102.0], [45.7, 102.0], [45.8, 102.0], [45.9, 102.0], [46.0, 102.0], [46.1, 102.0], [46.2, 102.0], [46.3, 102.0], [46.4, 102.0], [46.5, 102.0], [46.6, 102.0], [46.7, 102.0], [46.8, 102.0], [46.9, 102.0], [47.0, 102.0], [47.1, 102.0], [47.2, 102.0], [47.3, 102.0], [47.4, 102.0], [47.5, 103.0], [47.6, 103.0], [47.7, 103.0], [47.8, 103.0], [47.9, 103.0], [48.0, 103.0], [48.1, 103.0], [48.2, 103.0], [48.3, 103.0], [48.4, 103.0], [48.5, 103.0], [48.6, 103.0], [48.7, 103.0], [48.8, 103.0], [48.9, 103.0], [49.0, 103.0], [49.1, 103.0], [49.2, 103.0], [49.3, 103.0], [49.4, 103.0], [49.5, 103.0], [49.6, 103.0], [49.7, 103.0], [49.8, 103.0], [49.9, 103.0], [50.0, 103.0], [50.1, 103.0], [50.2, 103.0], [50.3, 103.0], [50.4, 103.0], [50.5, 103.0], [50.6, 103.0], [50.7, 103.0], [50.8, 104.0], [50.9, 104.0], [51.0, 104.0], [51.1, 104.0], [51.2, 104.0], [51.3, 104.0], [51.4, 104.0], [51.5, 104.0], [51.6, 104.0], [51.7, 104.0], [51.8, 104.0], [51.9, 104.0], [52.0, 104.0], [52.1, 104.0], [52.2, 104.0], [52.3, 104.0], [52.4, 104.0], [52.5, 104.0], [52.6, 104.0], [52.7, 104.0], [52.8, 104.0], [52.9, 104.0], [53.0, 104.0], [53.1, 104.0], [53.2, 104.0], [53.3, 104.0], [53.4, 104.0], [53.5, 104.0], [53.6, 104.0], [53.7, 104.0], [53.8, 105.0], [53.9, 105.0], [54.0, 105.0], [54.1, 105.0], [54.2, 105.0], [54.3, 105.0], [54.4, 105.0], [54.5, 105.0], [54.6, 105.0], [54.7, 105.0], [54.8, 105.0], [54.9, 105.0], [55.0, 105.0], [55.1, 105.0], [55.2, 105.0], [55.3, 105.0], [55.4, 105.0], [55.5, 105.0], [55.6, 105.0], [55.7, 105.0], [55.8, 105.0], [55.9, 105.0], [56.0, 105.0], [56.1, 105.0], [56.2, 105.0], [56.3, 105.0], [56.4, 105.0], [56.5, 105.0], [56.6, 105.0], [56.7, 106.0], [56.8, 106.0], [56.9, 106.0], [57.0, 106.0], [57.1, 106.0], [57.2, 106.0], [57.3, 106.0], [57.4, 106.0], [57.5, 106.0], [57.6, 106.0], [57.7, 106.0], [57.8, 106.0], [57.9, 106.0], [58.0, 106.0], [58.1, 106.0], [58.2, 106.0], [58.3, 106.0], [58.4, 106.0], [58.5, 106.0], [58.6, 106.0], [58.7, 106.0], [58.8, 106.0], [58.9, 106.0], [59.0, 106.0], [59.1, 106.0], [59.2, 106.0], [59.3, 107.0], [59.4, 107.0], [59.5, 107.0], [59.6, 107.0], [59.7, 107.0], [59.8, 107.0], [59.9, 107.0], [60.0, 107.0], [60.1, 107.0], [60.2, 107.0], [60.3, 107.0], [60.4, 107.0], [60.5, 107.0], [60.6, 107.0], [60.7, 107.0], [60.8, 107.0], [60.9, 107.0], [61.0, 107.0], [61.1, 107.0], [61.2, 107.0], [61.3, 107.0], [61.4, 107.0], [61.5, 107.0], [61.6, 107.0], [61.7, 107.0], [61.8, 108.0], [61.9, 108.0], [62.0, 108.0], [62.1, 108.0], [62.2, 108.0], [62.3, 108.0], [62.4, 108.0], [62.5, 108.0], [62.6, 108.0], [62.7, 108.0], [62.8, 108.0], [62.9, 108.0], [63.0, 108.0], [63.1, 108.0], [63.2, 108.0], [63.3, 108.0], [63.4, 108.0], [63.5, 108.0], [63.6, 108.0], [63.7, 108.0], [63.8, 108.0], [63.9, 108.0], [64.0, 108.0], [64.1, 108.0], [64.2, 108.0], [64.3, 109.0], [64.4, 109.0], [64.5, 109.0], [64.6, 109.0], [64.7, 109.0], [64.8, 109.0], [64.9, 109.0], [65.0, 109.0], [65.1, 109.0], [65.2, 109.0], [65.3, 109.0], [65.4, 109.0], [65.5, 109.0], [65.6, 109.0], [65.7, 109.0], [65.8, 109.0], [65.9, 109.0], [66.0, 109.0], [66.1, 109.0], [66.2, 109.0], [66.3, 109.0], [66.4, 109.0], [66.5, 109.0], [66.6, 109.0], [66.7, 110.0], [66.8, 110.0], [66.9, 110.0], [67.0, 110.0], [67.1, 110.0], [67.2, 110.0], [67.3, 110.0], [67.4, 110.0], [67.5, 110.0], [67.6, 110.0], [67.7, 110.0], [67.8, 110.0], [67.9, 110.0], [68.0, 110.0], [68.1, 110.0], [68.2, 110.0], [68.3, 110.0], [68.4, 110.0], [68.5, 110.0], [68.6, 110.0], [68.7, 110.0], [68.8, 111.0], [68.9, 111.0], [69.0, 111.0], [69.1, 111.0], [69.2, 111.0], [69.3, 111.0], [69.4, 111.0], [69.5, 111.0], [69.6, 111.0], [69.7, 111.0], [69.8, 111.0], [69.9, 111.0], [70.0, 111.0], [70.1, 111.0], [70.2, 111.0], [70.3, 111.0], [70.4, 111.0], [70.5, 111.0], [70.6, 111.0], [70.7, 111.0], [70.8, 111.0], [70.9, 112.0], [71.0, 112.0], [71.1, 112.0], [71.2, 112.0], [71.3, 112.0], [71.4, 112.0], [71.5, 112.0], [71.6, 112.0], [71.7, 112.0], [71.8, 112.0], [71.9, 112.0], [72.0, 112.0], [72.1, 112.0], [72.2, 112.0], [72.3, 112.0], [72.4, 112.0], [72.5, 112.0], [72.6, 112.0], [72.7, 112.0], [72.8, 113.0], [72.9, 113.0], [73.0, 113.0], [73.1, 113.0], [73.2, 113.0], [73.3, 113.0], [73.4, 113.0], [73.5, 113.0], [73.6, 113.0], [73.7, 113.0], [73.8, 113.0], [73.9, 113.0], [74.0, 113.0], [74.1, 113.0], [74.2, 113.0], [74.3, 113.0], [74.4, 113.0], [74.5, 113.0], [74.6, 114.0], [74.7, 114.0], [74.8, 114.0], [74.9, 114.0], [75.0, 114.0], [75.1, 114.0], [75.2, 114.0], [75.3, 114.0], [75.4, 114.0], [75.5, 114.0], [75.6, 114.0], [75.7, 114.0], [75.8, 114.0], [75.9, 114.0], [76.0, 114.0], [76.1, 114.0], [76.2, 114.0], [76.3, 115.0], [76.4, 115.0], [76.5, 115.0], [76.6, 115.0], [76.7, 115.0], [76.8, 115.0], [76.9, 115.0], [77.0, 115.0], [77.1, 115.0], [77.2, 115.0], [77.3, 115.0], [77.4, 115.0], [77.5, 115.0], [77.6, 115.0], [77.7, 115.0], [77.8, 115.0], [77.9, 115.0], [78.0, 116.0], [78.1, 116.0], [78.2, 116.0], [78.3, 116.0], [78.4, 116.0], [78.5, 116.0], [78.6, 116.0], [78.7, 116.0], [78.8, 116.0], [78.9, 116.0], [79.0, 116.0], [79.1, 116.0], [79.2, 116.0], [79.3, 116.0], [79.4, 116.0], [79.5, 116.0], [79.6, 117.0], [79.7, 117.0], [79.8, 117.0], [79.9, 117.0], [80.0, 117.0], [80.1, 117.0], [80.2, 117.0], [80.3, 117.0], [80.4, 117.0], [80.5, 117.0], [80.6, 117.0], [80.7, 117.0], [80.8, 117.0], [80.9, 117.0], [81.0, 117.0], [81.1, 118.0], [81.2, 118.0], [81.3, 118.0], [81.4, 118.0], [81.5, 118.0], [81.6, 118.0], [81.7, 118.0], [81.8, 118.0], [81.9, 118.0], [82.0, 118.0], [82.1, 118.0], [82.2, 118.0], [82.3, 118.0], [82.4, 118.0], [82.5, 118.0], [82.6, 119.0], [82.7, 119.0], [82.8, 119.0], [82.9, 119.0], [83.0, 119.0], [83.1, 119.0], [83.2, 119.0], [83.3, 119.0], [83.4, 119.0], [83.5, 119.0], [83.6, 119.0], [83.7, 119.0], [83.8, 119.0], [83.9, 120.0], [84.0, 120.0], [84.1, 120.0], [84.2, 120.0], [84.3, 120.0], [84.4, 120.0], [84.5, 120.0], [84.6, 120.0], [84.7, 120.0], [84.8, 120.0], [84.9, 120.0], [85.0, 120.0], [85.1, 121.0], [85.2, 121.0], [85.3, 121.0], [85.4, 121.0], [85.5, 121.0], [85.6, 121.0], [85.7, 121.0], [85.8, 121.0], [85.9, 121.0], [86.0, 121.0], [86.1, 121.0], [86.2, 122.0], [86.3, 122.0], [86.4, 122.0], [86.5, 122.0], [86.6, 122.0], [86.7, 122.0], [86.8, 122.0], [86.9, 122.0], [87.0, 122.0], [87.1, 122.0], [87.2, 122.0], [87.3, 123.0], [87.4, 123.0], [87.5, 123.0], [87.6, 123.0], [87.7, 123.0], [87.8, 123.0], [87.9, 123.0], [88.0, 123.0], [88.1, 123.0], [88.2, 123.0], [88.3, 124.0], [88.4, 124.0], [88.5, 124.0], [88.6, 124.0], [88.7, 124.0], [88.8, 124.0], [88.9, 124.0], [89.0, 124.0], [89.1, 124.0], [89.2, 125.0], [89.3, 125.0], [89.4, 125.0], [89.5, 125.0], [89.6, 125.0], [89.7, 125.0], [89.8, 125.0], [89.9, 125.0], [90.0, 126.0], [90.1, 126.0], [90.2, 126.0], [90.3, 126.0], [90.4, 126.0], [90.5, 126.0], [90.6, 126.0], [90.7, 126.0], [90.8, 127.0], [90.9, 127.0], [91.0, 127.0], [91.1, 127.0], [91.2, 127.0], [91.3, 127.0], [91.4, 127.0], [91.5, 128.0], [91.6, 128.0], [91.7, 128.0], [91.8, 128.0], [91.9, 128.0], [92.0, 128.0], [92.1, 128.0], [92.2, 129.0], [92.3, 129.0], [92.4, 129.0], [92.5, 129.0], [92.6, 129.0], [92.7, 130.0], [92.8, 130.0], [92.9, 130.0], [93.0, 130.0], [93.1, 130.0], [93.2, 130.0], [93.3, 131.0], [93.4, 131.0], [93.5, 131.0], [93.6, 131.0], [93.7, 131.0], [93.8, 132.0], [93.9, 132.0], [94.0, 132.0], [94.1, 132.0], [94.2, 132.0], [94.3, 133.0], [94.4, 133.0], [94.5, 133.0], [94.6, 133.0], [94.7, 133.0], [94.8, 134.0], [94.9, 134.0], [95.0, 134.0], [95.1, 134.0], [95.2, 135.0], [95.3, 135.0], [95.4, 135.0], [95.5, 136.0], [95.6, 136.0], [95.7, 136.0], [95.8, 136.0], [95.9, 137.0], [96.0, 137.0], [96.1, 137.0], [96.2, 138.0], [96.3, 138.0], [96.4, 138.0], [96.5, 139.0], [96.6, 139.0], [96.7, 139.0], [96.8, 140.0], [96.9, 140.0], [97.0, 141.0], [97.1, 141.0], [97.2, 142.0], [97.3, 142.0], [97.4, 143.0], [97.5, 143.0], [97.6, 144.0], [97.7, 144.0], [97.8, 145.0], [97.9, 145.0], [98.0, 146.0], [98.1, 147.0], [98.2, 148.0], [98.3, 149.0], [98.4, 149.0], [98.5, 150.0], [98.6, 151.0], [98.7, 152.0], [98.8, 153.0], [98.9, 154.0], [99.0, 156.0], [99.1, 157.0], [99.2, 159.0], [99.3, 161.0], [99.4, 163.0], [99.5, 166.0], [99.6, 170.0], [99.7, 175.0], [99.8, 186.0], [99.9, 309.0]], "isOverall": false, "label": "Process transactional inline data requests", "isController": false}], "supportsControllersDiscrimination": true, "maxX": 100.0, "title": "Response Time Percentiles"}},
        getOptions: function() {
            return {
                series: {
                    points: { show: false }
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimePercentiles'
                },
                xaxis: {
                    tickDecimals: 1,
                    axisLabel: "Percentiles",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Percentile value in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : %x.2 percentile was %y ms"
                },
                selection: { mode: "xy" },
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesResponseTimePercentiles"), function(series){
                series.curvedLines = {apply: true, tension: 1};
                series.threshold = {
                        below: percentileThreshold,
                        color: $("#slider-vertical").children("div").css("background-color")
                };
            });
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimesPercentiles"), dataset, options);
            // setup overview
            $.plot($("#overviewResponseTimesPercentiles"), dataset, prepareOverviewOptions(options));
        }
};

// Response times percentiles
function refreshResponseTimePercentiles() {
    var infos = responseTimePercentilesInfos;
    prepareSeries(infos.data);
    if (isGraph($("#flotResponseTimesPercentiles"))){
        infos.createGraph();
    } else {
        var choiceContainer = $("#choicesResponseTimePercentiles");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimesPercentiles", "#overviewResponseTimesPercentiles");
        $('#bodyResponseTimePercentiles .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
}

var responseTimeDistributionInfos = {
        data: {"result": {"minY": 1.0, "minX": 0.0, "maxY": 79926.0, "series": [{"data": [[0.0, 79926.0], [3000.0, 1.0], [1500.0, 2.0], [500.0, 45.0], [1000.0, 24.0], [2000.0, 2.0]], "isOverall": false, "label": "Process transactional inline data requests", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 500, "maxX": 3000.0, "title": "Response Time Distribution"}},
        getOptions: function() {
            var granularity = this.data.result.granularity;
            return {
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimeDistribution'
                },
                xaxis:{
                    axisLabel: "Response times in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of responses",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                bars : {
                    show: true,
                    barWidth: this.data.result.granularity
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: function(label, xval, yval, flotItem){
                        return yval + " responses for " + label + " were between " + xval + " and " + (xval + granularity) + " ms";
                    }
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimeDistribution"), prepareData(data.result.series, $("#choicesResponseTimeDistribution")), options);
        }

};

// Response time distribution
function refreshResponseTimeDistribution() {
    var infos = responseTimeDistributionInfos;
    prepareSeries(infos.data);
    if (isGraph($("#flotResponseTimeDistribution"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesResponseTimeDistribution");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        $('#footerResponseTimeDistribution .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var activeThreadsOverTimeInfos = {
        data: {"result": {"minY": 39.6186440677966, "minX": 1.50890946E12, "maxY": 50.0, "series": [{"data": [[1.50891804E12, 50.0], [1.50891156E12, 50.0], [1.50891378E12, 50.0], [1.508916E12, 50.0], [1.50891702E12, 50.0], [1.50891036E12, 50.0], [1.5089148E12, 50.0], [1.50891258E12, 50.0], [1.50891582E12, 50.0], [1.50891174E12, 50.0], [1.50891618E12, 50.0], [1.50891822E12, 50.0], [1.50891072E12, 50.0], [1.50891396E12, 50.0], [1.50891054E12, 50.0], [1.50891276E12, 50.0], [1.50891498E12, 50.0], [1.5089172E12, 50.0], [1.50890952E12, 50.0], [1.5089184E12, 50.0], [1.50891666E12, 50.0], [1.50891222E12, 50.0], [1.5089187E12, 50.0], [1.5089112E12, 50.0], [1.50891444E12, 50.0], [1.50891768E12, 50.0], [1.50891102E12, 50.0], [1.50891324E12, 50.0], [1.50891546E12, 50.0], [1.50891888E12, 50.0], [1.50891E12, 50.0], [1.5089136E12, 50.0], [1.50891138E12, 50.0], [1.50891462E12, 50.0], [1.50891684E12, 50.0], [1.50891786E12, 50.0], [1.50891342E12, 50.0], [1.5089124E12, 50.0], [1.50891906E12, 39.6186440677966], [1.50891018E12, 50.0], [1.50891564E12, 50.0], [1.5089157E12, 50.0], [1.50891126E12, 50.0], [1.50891774E12, 50.0], [1.50891024E12, 50.0], [1.50891348E12, 50.0], [1.5089145E12, 50.0], [1.50891672E12, 50.0], [1.50891894E12, 50.0], [1.50891006E12, 50.0], [1.50891228E12, 50.0], [1.50891792E12, 50.0], [1.50891042E12, 50.0], [1.50891264E12, 50.0], [1.50891366E12, 50.0], [1.50891588E12, 50.0], [1.50891246E12, 50.0], [1.5089169E12, 50.0], [1.50891468E12, 50.0], [1.50891144E12, 50.0], [1.5089181E12, 50.0], [1.5089109E12, 50.0], [1.50891312E12, 50.0], [1.50891414E12, 50.0], [1.50891636E12, 50.0], [1.50891294E12, 50.0], [1.50891738E12, 50.0], [1.50891516E12, 50.0], [1.5089097E12, 50.0], [1.50891192E12, 50.0], [1.50891858E12, 50.0], [1.50891108E12, 50.0], [1.50891552E12, 50.0], [1.5089133E12, 50.0], [1.50891654E12, 50.0], [1.50891876E12, 50.0], [1.50890988E12, 50.0], [1.50891432E12, 50.0], [1.5089121E12, 50.0], [1.50891534E12, 50.0], [1.50891756E12, 50.0], [1.50891318E12, 50.0], [1.50891762E12, 50.0], [1.5089154E12, 50.0], [1.50891864E12, 50.0], [1.50890994E12, 50.0], [1.50891216E12, 50.0], [1.50891198E12, 50.0], [1.50891642E12, 50.0], [1.50891096E12, 50.0], [1.5089142E12, 50.0], [1.50891456E12, 50.0], [1.50891012E12, 50.0], [1.50891234E12, 50.0], [1.50891558E12, 50.0], [1.50891882E12, 50.0], [1.5089178E12, 50.0], [1.50891114E12, 50.0], [1.50891336E12, 50.0], [1.5089166E12, 50.0], [1.50891438E12, 50.0], [1.5089106E12, 50.0], [1.50891504E12, 50.0], [1.50891282E12, 50.0], [1.50891606E12, 50.0], [1.50891384E12, 50.0], [1.50891828E12, 50.0], [1.50891162E12, 50.0], [1.50891486E12, 50.0], [1.50891708E12, 50.0], [1.50891078E12, 50.0], [1.508913E12, 50.0], [1.50891522E12, 50.0], [1.50891744E12, 50.0], [1.50890976E12, 50.0], [1.50890958E12, 50.0], [1.5089118E12, 50.0], [1.50891402E12, 50.0], [1.50891624E12, 50.0], [1.50891846E12, 50.0], [1.50891726E12, 50.0], [1.50890964E12, 50.0], [1.50891408E12, 50.0], [1.50891186E12, 50.0], [1.5089151E12, 50.0], [1.50891732E12, 50.0], [1.50891834E12, 50.0], [1.50891066E12, 50.0], [1.50891288E12, 50.0], [1.5089139E12, 50.0], [1.50891612E12, 50.0], [1.50891852E12, 50.0], [1.50890982E12, 50.0], [1.50891204E12, 50.0], [1.50891426E12, 50.0], [1.50891648E12, 50.0], [1.5089175E12, 50.0], [1.50891084E12, 50.0], [1.50891528E12, 50.0], [1.50891306E12, 50.0], [1.5089163E12, 50.0], [1.508919E12, 50.0], [1.50891252E12, 50.0], [1.50891474E12, 50.0], [1.50891696E12, 50.0], [1.5089103E12, 50.0], [1.50891132E12, 50.0], [1.50891354E12, 50.0], [1.50891576E12, 50.0], [1.50891798E12, 50.0], [1.50891678E12, 50.0], [1.5089127E12, 50.0], [1.50891714E12, 50.0], [1.50891492E12, 50.0], [1.50891816E12, 50.0], [1.50890946E12, 50.0], [1.50891168E12, 50.0], [1.5089115E12, 50.0], [1.50891594E12, 50.0], [1.50891048E12, 50.0], [1.50891372E12, 50.0]], "isOverall": false, "label": "ProcessRequestTransactionalInlineDataOnly", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.50891906E12, "title": "Active Threads Over Time"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: "%H:%M:%S",
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of active threads",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 6,
                    show: true,
                    container: '#legendActiveThreadsOverTime'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                selection: {
                    mode: 'xy'
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : At %x there were %y active threads"
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesActiveThreadsOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotActiveThreadsOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewActiveThreadsOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Active Threads Over Time
function refreshActiveThreadsOverTime(fixTimestamps) {
    var infos = activeThreadsOverTimeInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -18000000);
    }
    if(isGraph($("#flotActiveThreadsOverTime"))) {
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesActiveThreadsOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotActiveThreadsOverTime", "#overviewActiveThreadsOverTime");
        $('#footerActiveThreadsOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var timeVsThreadsInfos = {
        data: {"result": {"minY": 91.0, "minX": 1.0, "maxY": 139.0, "series": [{"data": [[2.0, 135.0], [3.0, 112.0], [4.0, 100.0], [5.0, 111.0], [6.0, 93.0], [7.0, 102.0], [8.0, 107.0], [9.0, 93.0], [10.0, 104.0], [11.0, 107.0], [12.0, 104.0], [13.0, 97.0], [14.0, 112.0], [15.0, 95.0], [16.0, 102.0], [17.0, 100.0], [18.0, 91.0], [19.0, 100.0], [20.0, 91.0], [21.0, 106.0], [22.0, 97.0], [23.0, 96.0], [24.0, 119.0], [25.0, 108.0], [26.0, 106.0], [27.0, 91.0], [28.0, 102.0], [29.0, 95.0], [30.0, 94.0], [31.0, 104.0], [33.0, 139.0], [32.0, 114.0], [35.0, 113.0], [34.0, 99.0], [37.0, 108.0], [36.0, 106.0], [39.0, 92.0], [38.0, 96.0], [41.0, 98.0], [40.0, 98.0], [43.0, 100.0], [42.0, 99.0], [45.0, 93.0], [44.0, 101.0], [47.0, 136.0], [46.0, 99.0], [49.0, 102.0], [48.0, 103.0], [50.0, 107.85996422808859], [1.0, 112.0]], "isOverall": false, "label": "Process transactional inline data requests", "isController": false}, {"data": [[49.984687500000014, 107.8574249999989]], "isOverall": false, "label": "Process transactional inline data requests-Aggregated", "isController": false}], "supportsControllersDiscrimination": true, "maxX": 50.0, "title": "Time VS Threads"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    axisLabel: "Number of active threads",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average response times in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: { noColumns: 2,show: true, container: '#legendTimeVsThreads' },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s: At %x.2 active threads, Average response time was %y.2 ms"
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesTimeVsThreads"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotTimesVsThreads"), dataset, options);
            // setup overview
            $.plot($("#overviewTimesVsThreads"), dataset, prepareOverviewOptions(options));
        }
};

// Time vs threads
function refreshTimeVsThreads(){
    var infos = timeVsThreadsInfos;
    prepareSeries(infos.data);
    if(isGraph($("#flotTimesVsThreads"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesTimeVsThreads");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotTimesVsThreads", "#overviewTimesVsThreads");
        $('#footerTimeVsThreads .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var bytesThroughputOverTimeInfos = {
        data : {"result": {"minY": 0.0, "minX": 1.50890946E12, "maxY": 10249.166666666666, "series": [{"data": [[1.50891804E12, 0.0], [1.50891156E12, 0.0], [1.50891378E12, 0.0], [1.508916E12, 0.0], [1.50891702E12, 0.0], [1.50891036E12, 0.0], [1.5089148E12, 0.0], [1.50891258E12, 0.0], [1.50891582E12, 0.0], [1.50891174E12, 0.0], [1.50891618E12, 0.0], [1.50891822E12, 0.0], [1.50891072E12, 0.0], [1.50891396E12, 0.0], [1.50891054E12, 0.0], [1.50891276E12, 0.0], [1.50891498E12, 0.0], [1.5089172E12, 0.0], [1.50890952E12, 0.0], [1.5089184E12, 0.0], [1.50891666E12, 0.0], [1.50891222E12, 0.0], [1.5089187E12, 0.0], [1.5089112E12, 0.0], [1.50891444E12, 0.0], [1.50891768E12, 0.0], [1.50891102E12, 0.0], [1.50891324E12, 0.0], [1.50891546E12, 0.0], [1.50891888E12, 0.0], [1.50891E12, 0.0], [1.5089136E12, 0.0], [1.50891138E12, 0.0], [1.50891462E12, 0.0], [1.50891684E12, 0.0], [1.50891786E12, 0.0], [1.50891342E12, 0.0], [1.5089124E12, 0.0], [1.50891906E12, 0.0], [1.50891018E12, 0.0], [1.50891564E12, 0.0], [1.5089157E12, 0.0], [1.50891126E12, 0.0], [1.50891774E12, 0.0], [1.50891024E12, 0.0], [1.50891348E12, 0.0], [1.5089145E12, 0.0], [1.50891672E12, 0.0], [1.50891894E12, 0.0], [1.50891006E12, 0.0], [1.50891228E12, 0.0], [1.50891792E12, 0.0], [1.50891042E12, 0.0], [1.50891264E12, 0.0], [1.50891366E12, 0.0], [1.50891588E12, 0.0], [1.50891246E12, 0.0], [1.5089169E12, 0.0], [1.50891468E12, 0.0], [1.50891144E12, 0.0], [1.5089181E12, 0.0], [1.5089109E12, 0.0], [1.50891312E12, 0.0], [1.50891414E12, 0.0], [1.50891636E12, 0.0], [1.50891294E12, 0.0], [1.50891738E12, 0.0], [1.50891516E12, 0.0], [1.5089097E12, 0.0], [1.50891192E12, 0.0], [1.50891858E12, 0.0], [1.50891108E12, 0.0], [1.50891552E12, 0.0], [1.5089133E12, 0.0], [1.50891654E12, 0.0], [1.50891876E12, 0.0], [1.50890988E12, 0.0], [1.50891432E12, 0.0], [1.5089121E12, 0.0], [1.50891534E12, 0.0], [1.50891756E12, 0.0], [1.50891318E12, 0.0], [1.50891762E12, 0.0], [1.5089154E12, 0.0], [1.50891864E12, 0.0], [1.50890994E12, 0.0], [1.50891216E12, 0.0], [1.50891198E12, 0.0], [1.50891642E12, 0.0], [1.50891096E12, 0.0], [1.5089142E12, 0.0], [1.50891456E12, 0.0], [1.50891012E12, 0.0], [1.50891234E12, 0.0], [1.50891558E12, 0.0], [1.50891882E12, 0.0], [1.5089178E12, 0.0], [1.50891114E12, 0.0], [1.50891336E12, 0.0], [1.5089166E12, 0.0], [1.50891438E12, 0.0], [1.5089106E12, 0.0], [1.50891504E12, 0.0], [1.50891282E12, 0.0], [1.50891606E12, 0.0], [1.50891384E12, 0.0], [1.50891828E12, 0.0], [1.50891162E12, 0.0], [1.50891486E12, 0.0], [1.50891708E12, 0.0], [1.50891078E12, 0.0], [1.508913E12, 0.0], [1.50891522E12, 0.0], [1.50891744E12, 0.0], [1.50890976E12, 0.0], [1.50890958E12, 0.0], [1.5089118E12, 0.0], [1.50891402E12, 0.0], [1.50891624E12, 0.0], [1.50891846E12, 0.0], [1.50891726E12, 0.0], [1.50890964E12, 0.0], [1.50891408E12, 0.0], [1.50891186E12, 0.0], [1.5089151E12, 0.0], [1.50891732E12, 0.0], [1.50891834E12, 0.0], [1.50891066E12, 0.0], [1.50891288E12, 0.0], [1.5089139E12, 0.0], [1.50891612E12, 0.0], [1.50891852E12, 0.0], [1.50890982E12, 0.0], [1.50891204E12, 0.0], [1.50891426E12, 0.0], [1.50891648E12, 0.0], [1.5089175E12, 0.0], [1.50891084E12, 0.0], [1.50891528E12, 0.0], [1.50891306E12, 0.0], [1.5089163E12, 0.0], [1.508919E12, 0.0], [1.50891252E12, 0.0], [1.50891474E12, 0.0], [1.50891696E12, 0.0], [1.5089103E12, 0.0], [1.50891132E12, 0.0], [1.50891354E12, 0.0], [1.50891576E12, 0.0], [1.50891798E12, 0.0], [1.50891678E12, 0.0], [1.5089127E12, 0.0], [1.50891714E12, 0.0], [1.50891492E12, 0.0], [1.50891816E12, 0.0], [1.50890946E12, 0.0], [1.50891168E12, 0.0], [1.5089115E12, 0.0], [1.50891594E12, 0.0], [1.50891048E12, 0.0], [1.50891372E12, 0.0]], "isOverall": false, "label": "Bytes received per second", "isController": false}, {"data": [[1.50891804E12, 10187.916666666666], [1.50891156E12, 10208.333333333334], [1.50891378E12, 10208.333333333334], [1.508916E12, 10208.333333333334], [1.50891702E12, 10208.333333333334], [1.50891036E12, 10200.0], [1.5089148E12, 10208.333333333334], [1.50891258E12, 10228.75], [1.50891582E12, 10228.75], [1.50891174E12, 10187.916666666666], [1.50891618E12, 10208.333333333334], [1.50891822E12, 10208.333333333334], [1.50891072E12, 10208.333333333334], [1.50891396E12, 10208.333333333334], [1.50891054E12, 10200.0], [1.50891276E12, 10208.333333333334], [1.50891498E12, 10208.333333333334], [1.5089172E12, 10208.333333333334], [1.50890952E12, 10191.666666666666], [1.5089184E12, 10208.333333333334], [1.50891666E12, 10208.333333333334], [1.50891222E12, 10208.333333333334], [1.5089187E12, 10208.333333333334], [1.5089112E12, 10187.916666666666], [1.50891444E12, 10208.333333333334], [1.50891768E12, 10208.333333333334], [1.50891102E12, 10208.333333333334], [1.50891324E12, 10208.333333333334], [1.50891546E12, 10208.333333333334], [1.50891888E12, 10208.333333333334], [1.50891E12, 10200.0], [1.5089136E12, 10208.333333333334], [1.50891138E12, 10208.333333333334], [1.50891462E12, 10228.75], [1.50891684E12, 10208.333333333334], [1.50891786E12, 10228.75], [1.50891342E12, 10208.333333333334], [1.5089124E12, 10208.333333333334], [1.50891906E12, 2409.1666666666665], [1.50891018E12, 10220.4], [1.50891564E12, 10208.333333333334], [1.5089157E12, 10208.333333333334], [1.50891126E12, 10208.333333333334], [1.50891774E12, 10208.333333333334], [1.50891024E12, 10179.6], [1.50891348E12, 10208.333333333334], [1.5089145E12, 10208.333333333334], [1.50891672E12, 10208.333333333334], [1.50891894E12, 10208.333333333334], [1.50891006E12, 10200.0], [1.50891228E12, 10208.333333333334], [1.50891792E12, 10187.916666666666], [1.50891042E12, 10200.0], [1.50891264E12, 10187.916666666666], [1.50891366E12, 10208.333333333334], [1.50891588E12, 10187.916666666666], [1.50891246E12, 10208.333333333334], [1.5089169E12, 10208.333333333334], [1.50891468E12, 10187.916666666666], [1.50891144E12, 10208.333333333334], [1.5089181E12, 10208.333333333334], [1.5089109E12, 10208.333333333334], [1.50891312E12, 10208.333333333334], [1.50891414E12, 10187.916666666666], [1.50891636E12, 10208.333333333334], [1.50891294E12, 10167.5], [1.50891738E12, 10208.333333333334], [1.50891516E12, 10208.333333333334], [1.5089097E12, 10200.0], [1.50891192E12, 10208.333333333334], [1.50891858E12, 10208.333333333334], [1.50891108E12, 10208.333333333334], [1.50891552E12, 10208.333333333334], [1.5089133E12, 10208.333333333334], [1.50891654E12, 10208.333333333334], [1.50891876E12, 10208.333333333334], [1.50890988E12, 10200.0], [1.50891432E12, 10208.333333333334], [1.5089121E12, 10208.333333333334], [1.50891534E12, 10187.916666666666], [1.50891756E12, 10208.333333333334], [1.50891318E12, 10187.916666666666], [1.50891762E12, 10208.333333333334], [1.5089154E12, 10208.333333333334], [1.50891864E12, 10208.333333333334], [1.50890994E12, 10200.0], [1.50891216E12, 10208.333333333334], [1.50891198E12, 10249.166666666666], [1.50891642E12, 10208.333333333334], [1.50891096E12, 10208.333333333334], [1.5089142E12, 10208.333333333334], [1.50891456E12, 10208.333333333334], [1.50891012E12, 10200.0], [1.50891234E12, 10208.333333333334], [1.50891558E12, 10208.333333333334], [1.50891882E12, 10208.333333333334], [1.5089178E12, 10208.333333333334], [1.50891114E12, 10228.75], [1.50891336E12, 10208.333333333334], [1.5089166E12, 10208.333333333334], [1.50891438E12, 10208.333333333334], [1.5089106E12, 10200.0], [1.50891504E12, 10208.333333333334], [1.50891282E12, 10228.75], [1.50891606E12, 10228.75], [1.50891384E12, 10208.333333333334], [1.50891828E12, 10208.333333333334], [1.50891162E12, 10208.333333333334], [1.50891486E12, 10208.333333333334], [1.50891708E12, 10208.333333333334], [1.50891078E12, 10208.333333333334], [1.508913E12, 10228.75], [1.50891522E12, 10208.333333333334], [1.50891744E12, 10208.333333333334], [1.50890976E12, 10200.0], [1.50890958E12, 10198.05], [1.5089118E12, 10208.333333333334], [1.50891402E12, 10208.333333333334], [1.50891624E12, 10208.333333333334], [1.50891846E12, 10208.333333333334], [1.50891726E12, 10208.333333333334], [1.50890964E12, 10200.0], [1.50891408E12, 10228.75], [1.50891186E12, 10208.333333333334], [1.5089151E12, 10208.333333333334], [1.50891732E12, 10208.333333333334], [1.50891834E12, 10208.333333333334], [1.50891066E12, 10206.383333333333], [1.50891288E12, 10228.75], [1.5089139E12, 10208.333333333334], [1.50891612E12, 10187.916666666666], [1.50891852E12, 10208.333333333334], [1.50890982E12, 10200.0], [1.50891204E12, 10167.5], [1.50891426E12, 10208.333333333334], [1.50891648E12, 10208.333333333334], [1.5089175E12, 10208.333333333334], [1.50891084E12, 10208.333333333334], [1.50891528E12, 10228.75], [1.50891306E12, 10208.333333333334], [1.5089163E12, 10208.333333333334], [1.508919E12, 10208.333333333334], [1.50891252E12, 10208.333333333334], [1.50891474E12, 10208.333333333334], [1.50891696E12, 10208.333333333334], [1.5089103E12, 10200.0], [1.50891132E12, 10208.333333333334], [1.50891354E12, 10208.333333333334], [1.50891576E12, 10208.333333333334], [1.50891798E12, 10228.75], [1.50891678E12, 10208.333333333334], [1.5089127E12, 10208.333333333334], [1.50891714E12, 10208.333333333334], [1.50891492E12, 10208.333333333334], [1.50891816E12, 10208.333333333334], [1.50890946E12, 7784.633333333333], [1.50891168E12, 10228.75], [1.5089115E12, 10208.333333333334], [1.50891594E12, 10208.333333333334], [1.50891048E12, 10200.0], [1.50891372E12, 10208.333333333334]], "isOverall": false, "label": "Bytes sent per second", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.50891906E12, "title": "Bytes Throughput Over Time"}},
        getOptions : function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: "%H:%M:%S",
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity) ,
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Bytes/sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendBytesThroughputOverTime'
                },
                selection: {
                    mode: "xy"
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y"
                }
            };
        },
        createGraph : function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesBytesThroughputOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotBytesThroughputOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewBytesThroughputOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Bytes throughput Over Time
function refreshBytesThroughputOverTime(fixTimestamps) {
    var infos = bytesThroughputOverTimeInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -18000000);
    }
    if(isGraph($("#flotBytesThroughputOverTime"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesBytesThroughputOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotBytesThroughputOverTime", "#overviewBytesThroughputOverTime");
        $('#footerBytesThroughputOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
}

var responseTimesOverTimeInfos = {
        data: {"result": {"minY": 98.41200000000002, "minX": 1.50890946E12, "maxY": 225.68586387434559, "series": [{"data": [[1.50891804E12, 101.84168336673345], [1.50891156E12, 106.83199999999997], [1.50891378E12, 111.62999999999997], [1.508916E12, 110.22400000000002], [1.50891702E12, 100.10999999999997], [1.50891036E12, 104.46], [1.5089148E12, 124.5], [1.50891258E12, 108.71457085828341], [1.50891582E12, 105.17764471057883], [1.50891174E12, 107.21643286573142], [1.50891618E12, 103.67400000000004], [1.50891822E12, 100.80599999999994], [1.50891072E12, 107.24800000000006], [1.50891396E12, 112.44999999999996], [1.50891054E12, 109.34400000000001], [1.50891276E12, 113.56200000000003], [1.50891498E12, 114.72999999999995], [1.5089172E12, 101.96399999999994], [1.50890952E12, 111.432], [1.5089184E12, 107.39600000000007], [1.50891666E12, 103.18600000000005], [1.50891222E12, 109.36799999999997], [1.5089187E12, 103.32800000000005], [1.5089112E12, 112.04809619238476], [1.50891444E12, 111.09599999999993], [1.50891768E12, 100.23800000000001], [1.50891102E12, 105.18999999999993], [1.50891324E12, 108.96599999999992], [1.50891546E12, 107.34799999999994], [1.50891888E12, 104.64799999999998], [1.50891E12, 106.22200000000002], [1.5089136E12, 124.95200000000003], [1.50891138E12, 105.68800000000003], [1.50891462E12, 110.3413173652695], [1.50891684E12, 99.8], [1.50891786E12, 101.61676646706584], [1.50891342E12, 114.132], [1.5089124E12, 112.13199999999998], [1.50891906E12, 104.64406779661019], [1.50891018E12, 104.77045908183628], [1.50891564E12, 104.35799999999999], [1.5089157E12, 107.42200000000001], [1.50891126E12, 106.73000000000005], [1.50891774E12, 99.40200000000007], [1.50891024E12, 103.71142284569135], [1.50891348E12, 122.87400000000002], [1.5089145E12, 111.93199999999997], [1.50891672E12, 100.63199999999998], [1.50891894E12, 105.73799999999996], [1.50891006E12, 105.04999999999994], [1.50891228E12, 108.92399999999999], [1.50891792E12, 99.71342685370747], [1.50891042E12, 102.70600000000003], [1.50891264E12, 111.0701402805611], [1.50891366E12, 116.43799999999997], [1.50891588E12, 103.6012024048096], [1.50891246E12, 109.20799999999996], [1.5089169E12, 100.70800000000006], [1.50891468E12, 109.67535070140278], [1.50891144E12, 107.35199999999993], [1.5089181E12, 103.48800000000001], [1.5089109E12, 107.27199999999992], [1.50891312E12, 108.09599999999995], [1.50891414E12, 111.11623246492988], [1.50891636E12, 101.97000000000003], [1.50891294E12, 108.50000000000007], [1.50891738E12, 98.81799999999993], [1.50891516E12, 113.23400000000008], [1.5089097E12, 108.404], [1.50891192E12, 106.28400000000002], [1.50891858E12, 105.26199999999992], [1.50891108E12, 104.36599999999987], [1.50891552E12, 108.62199999999999], [1.5089133E12, 109.90399999999988], [1.50891654E12, 103.80799999999992], [1.50891876E12, 103.94600000000007], [1.50890988E12, 105.87400000000001], [1.50891432E12, 112.40800000000003], [1.5089121E12, 107.44599999999997], [1.50891534E12, 109.19839679358724], [1.50891756E12, 100.54199999999994], [1.50891318E12, 107.50501002004003], [1.50891762E12, 99.21400000000001], [1.5089154E12, 110.17599999999993], [1.50891864E12, 102.42599999999996], [1.50890994E12, 107.05800000000004], [1.50891216E12, 107.25999999999998], [1.50891198E12, 114.02988047808763], [1.50891642E12, 104.04599999999994], [1.50891096E12, 104.15800000000003], [1.5089142E12, 115.63600000000001], [1.50891456E12, 111.99], [1.50891012E12, 103.80999999999997], [1.50891234E12, 108.19400000000013], [1.50891558E12, 106.31199999999997], [1.50891882E12, 104.26599999999998], [1.5089178E12, 101.366], [1.50891114E12, 104.85429141716565], [1.50891336E12, 110.48599999999996], [1.5089166E12, 109.42199999999997], [1.50891438E12, 112.03600000000009], [1.5089106E12, 106.55800000000006], [1.50891504E12, 115.11399999999999], [1.50891282E12, 110.27345309381238], [1.50891606E12, 106.01996007984026], [1.50891384E12, 111.20800000000011], [1.50891828E12, 99.60600000000008], [1.50891162E12, 104.80000000000008], [1.50891486E12, 118.356], [1.50891708E12, 99.11600000000004], [1.50891078E12, 105.79200000000006], [1.508913E12, 116.82435129740522], [1.50891522E12, 111.17600000000002], [1.50891744E12, 98.93399999999995], [1.50890976E12, 105.87000000000009], [1.50890958E12, 107.99799999999993], [1.5089118E12, 107.90199999999993], [1.50891402E12, 110.93399999999995], [1.50891624E12, 102.80800000000005], [1.50891846E12, 105.19000000000011], [1.50891726E12, 101.98199999999999], [1.50890964E12, 108.25000000000001], [1.50891408E12, 111.3213572854291], [1.50891186E12, 107.02999999999996], [1.5089151E12, 113.63000000000012], [1.50891732E12, 101.14199999999998], [1.50891834E12, 98.41200000000002], [1.50891066E12, 106.60600000000002], [1.50891288E12, 122.79041916167662], [1.5089139E12, 114.40600000000002], [1.50891612E12, 103.98396793587176], [1.50891852E12, 104.56400000000002], [1.50890982E12, 104.95000000000005], [1.50891204E12, 108.59839357429712], [1.50891426E12, 112.64400000000005], [1.50891648E12, 103.01200000000001], [1.5089175E12, 101.69400000000003], [1.50891084E12, 105.85000000000005], [1.50891528E12, 112.58882235528938], [1.50891306E12, 108.60399999999997], [1.5089163E12, 103.846], [1.508919E12, 106.60999999999996], [1.50891252E12, 108.88199999999995], [1.50891474E12, 110.69399999999996], [1.50891696E12, 99.02999999999996], [1.5089103E12, 105.71000000000005], [1.50891132E12, 108.07000000000001], [1.50891354E12, 110.85599999999997], [1.50891576E12, 106.282], [1.50891798E12, 103.44710578842317], [1.50891678E12, 98.6619999999999], [1.5089127E12, 116.19799999999996], [1.50891714E12, 98.98], [1.50891492E12, 119.43399999999994], [1.50891816E12, 102.86999999999999], [1.50890946E12, 225.68586387434559], [1.50891168E12, 106.56487025948108], [1.5089115E12, 108.17400000000008], [1.50891594E12, 104.712], [1.50891048E12, 103.21200000000006], [1.50891372E12, 112.78600000000002]], "isOverall": false, "label": "Process transactional inline data requests", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.50891906E12, "title": "Response Time Over Time"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: "%H:%M:%S",
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average response time in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimesOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Average response time was %y ms"
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesResponseTimesOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimesOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewResponseTimesOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Response Times Over Time
function refreshResponseTimeOverTime(fixTimestamps) {
    var infos = responseTimesOverTimeInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -18000000);
    }
    if(isGraph($("#flotResponseTimesOverTime"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesResponseTimesOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimesOverTime", "#overviewResponseTimesOverTime");
        $('#footerResponseTimesOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var latenciesOverTimeInfos = {
        data: {"result": {"minY": 98.35000000000005, "minX": 1.50890946E12, "maxY": 225.61518324607334, "series": [{"data": [[1.50891804E12, 101.79358717434869], [1.50891156E12, 106.75799999999992], [1.50891378E12, 111.56799999999993], [1.508916E12, 110.16599999999997], [1.50891702E12, 100.03800000000003], [1.50891036E12, 104.396], [1.5089148E12, 124.41400000000009], [1.50891258E12, 108.63273453093818], [1.50891582E12, 105.127744510978], [1.50891174E12, 107.14829659318639], [1.50891618E12, 103.60999999999989], [1.50891822E12, 100.74999999999996], [1.50891072E12, 107.18200000000002], [1.50891396E12, 112.38000000000001], [1.50891054E12, 109.28599999999996], [1.50891276E12, 113.46800000000012], [1.50891498E12, 114.67199999999994], [1.5089172E12, 101.88399999999999], [1.50890952E12, 111.36399999999995], [1.5089184E12, 107.31999999999995], [1.50891666E12, 103.11799999999998], [1.50891222E12, 109.29999999999998], [1.5089187E12, 103.25600000000006], [1.5089112E12, 111.9779559118237], [1.50891444E12, 111.05199999999994], [1.50891768E12, 100.17600000000002], [1.50891102E12, 105.10400000000003], [1.50891324E12, 108.90399999999995], [1.50891546E12, 107.26400000000001], [1.50891888E12, 104.57800000000009], [1.50891E12, 106.16000000000004], [1.5089136E12, 124.88799999999996], [1.50891138E12, 105.60400000000001], [1.50891462E12, 110.27145708582833], [1.50891684E12, 99.74600000000001], [1.50891786E12, 101.56287425149702], [1.50891342E12, 114.06599999999999], [1.5089124E12, 112.06400000000006], [1.50891906E12, 104.60169491525424], [1.50891018E12, 104.67864271457086], [1.50891564E12, 104.30800000000012], [1.5089157E12, 107.33399999999995], [1.50891126E12, 106.67600000000002], [1.50891774E12, 99.34200000000006], [1.50891024E12, 103.64529058116244], [1.50891348E12, 122.82199999999999], [1.5089145E12, 111.86800000000005], [1.50891672E12, 100.55799999999992], [1.50891894E12, 105.67599999999999], [1.50891006E12, 105.01000000000003], [1.50891228E12, 108.862], [1.50891792E12, 99.62525050100189], [1.50891042E12, 102.63999999999999], [1.50891264E12, 111.00601202404809], [1.50891366E12, 116.372], [1.50891588E12, 103.51703406813631], [1.50891246E12, 109.14400000000002], [1.5089169E12, 100.64600000000006], [1.50891468E12, 109.59719438877757], [1.50891144E12, 107.30400000000006], [1.5089181E12, 103.40799999999997], [1.5089109E12, 107.212], [1.50891312E12, 108.0440000000001], [1.50891414E12, 111.04809619238478], [1.50891636E12, 101.91600000000008], [1.50891294E12, 108.42369477911647], [1.50891738E12, 98.76599999999995], [1.50891516E12, 113.1660000000001], [1.5089097E12, 108.34999999999998], [1.50891192E12, 106.20800000000004], [1.50891858E12, 105.21399999999997], [1.50891108E12, 104.31000000000013], [1.50891552E12, 108.56000000000007], [1.5089133E12, 109.83399999999999], [1.50891654E12, 103.74600000000004], [1.50891876E12, 103.87999999999992], [1.50890988E12, 105.81800000000001], [1.50891432E12, 112.37400000000002], [1.5089121E12, 107.38599999999991], [1.50891534E12, 109.11823647294595], [1.50891756E12, 100.49000000000007], [1.50891318E12, 107.4529058116233], [1.50891762E12, 99.16799999999999], [1.5089154E12, 110.10200000000003], [1.50891864E12, 102.34400000000002], [1.50890994E12, 106.98800000000004], [1.50891216E12, 107.20200000000001], [1.50891198E12, 113.94223107569726], [1.50891642E12, 103.98200000000007], [1.50891096E12, 104.08000000000008], [1.5089142E12, 115.5740000000001], [1.50891456E12, 111.91799999999999], [1.50891012E12, 103.74399999999997], [1.50891234E12, 108.13000000000001], [1.50891558E12, 106.25600000000007], [1.50891882E12, 104.198], [1.5089178E12, 101.29400000000008], [1.50891114E12, 104.79840319361278], [1.50891336E12, 110.42000000000002], [1.5089166E12, 109.35999999999993], [1.50891438E12, 111.98199999999994], [1.5089106E12, 106.4879999999999], [1.50891504E12, 115.04200000000006], [1.50891282E12, 110.1596806387226], [1.50891606E12, 105.9840319361278], [1.50891384E12, 111.15999999999994], [1.50891828E12, 99.54600000000008], [1.50891162E12, 104.73199999999989], [1.50891486E12, 118.27799999999998], [1.50891708E12, 99.06200000000011], [1.50891078E12, 105.73799999999994], [1.508913E12, 116.74451097804395], [1.50891522E12, 111.08599999999996], [1.50891744E12, 98.86799999999998], [1.50890976E12, 105.80399999999992], [1.50890958E12, 107.92599999999989], [1.5089118E12, 107.85600000000011], [1.50891402E12, 110.86000000000007], [1.50891624E12, 102.744], [1.50891846E12, 105.12400000000002], [1.50891726E12, 101.90599999999998], [1.50890964E12, 108.16999999999996], [1.50891408E12, 111.25349301397199], [1.50891186E12, 106.96399999999998], [1.5089151E12, 113.5539999999999], [1.50891732E12, 101.05000000000007], [1.50891834E12, 98.35000000000005], [1.50891066E12, 106.5339999999999], [1.50891288E12, 122.71856287425152], [1.5089139E12, 114.31800000000008], [1.50891612E12, 103.92384769539075], [1.50891852E12, 104.49999999999989], [1.50890982E12, 104.89200000000008], [1.50891204E12, 108.53212851405634], [1.50891426E12, 112.58200000000006], [1.50891648E12, 102.95400000000002], [1.5089175E12, 101.61799999999997], [1.50891084E12, 105.78599999999996], [1.50891528E12, 112.51497005988031], [1.50891306E12, 108.5420000000001], [1.5089163E12, 103.77400000000003], [1.508919E12, 106.54199999999997], [1.50891252E12, 108.82600000000008], [1.50891474E12, 110.63400000000006], [1.50891696E12, 98.96200000000003], [1.5089103E12, 105.648], [1.50891132E12, 108.01400000000005], [1.50891354E12, 110.80599999999991], [1.50891576E12, 106.21800000000003], [1.50891798E12, 103.38722554890215], [1.50891678E12, 98.61200000000008], [1.5089127E12, 116.13199999999988], [1.50891714E12, 98.91600000000001], [1.50891492E12, 119.36400000000009], [1.50891816E12, 102.80599999999991], [1.50890946E12, 225.61518324607334], [1.50891168E12, 106.52694610778438], [1.5089115E12, 108.086], [1.50891594E12, 104.64600000000002], [1.50891048E12, 103.144], [1.50891372E12, 112.72999999999992]], "isOverall": false, "label": "Process transactional inline data requests", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.50891906E12, "title": "Latencies Over Time"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: "%H:%M:%S",
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average Response latencies in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendLatenciesOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Average latency was %y ms"
                }
            };
        },
        createGraph: function () {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesLatenciesOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotLatenciesOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewLatenciesOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Latencies Over Time
function refreshLatenciesOverTime(fixTimestamps) {
    var infos = latenciesOverTimeInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -18000000);
    }
    if(isGraph($("#flotLatenciesOverTime"))) {
        infos.createGraph();
    }else {
        var choiceContainer = $("#choicesLatenciesOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotLatenciesOverTime", "#overviewLatenciesOverTime");
        $('#footerLatenciesOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var responseTimeVsRequestInfos = {
    data: {"result": {"minY": 101.5, "minX": 118.0, "maxY": 118.0, "series": [{"data": [[382.0, 118.0], [118.0, 101.5], [500.0, 103.0], [501.0, 104.0], [499.0, 103.0], [502.0, 105.0], [498.0, 104.0]], "isOverall": false, "label": "Successes", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 502.0, "title": "Response Time Vs Request"}},
    getOptions: function() {
        return {
            series: {
                lines: {
                    show: false
                },
                points: {
                    show: true
                }
            },
            xaxis: {
                axisLabel: "Global number of requests per second",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            yaxis: {
                axisLabel: "Median Response Time (ms)",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            legend: {
                noColumns: 2,
                show: true,
                container: '#legendResponseTimeVsRequest'
            },
            selection: {
                mode: 'xy'
            },
            grid: {
                hoverable: true // IMPORTANT! this is needed for tooltip to work
            },
            tooltip: true,
            tooltipOpts: {
                content: "%s : Median response time at %x req/s was %y ms"
            }
        };
    },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesResponseTimeVsRequest"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotResponseTimeVsRequest"), dataset, options);
        // setup overview
        $.plot($("#overviewResponseTimeVsRequest"), dataset, prepareOverviewOptions(options));

    }
};

// Response Time vs Request
function refreshResponseTimeVsRequest() {
    var infos = responseTimeVsRequestInfos;
    prepareSeries(infos.data);
    if (isGraph($("#flotResponseTimeVsRequest"))){
        infos.create();
    }else{
        var choiceContainer = $("#choicesResponseTimeVsRequest");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimeVsRequest", "#overviewResponseTimeVsRequest");
        $('#footerResponseRimeVsRequest .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};


var latenciesVsRequestInfos = {
    data: {"result": {"minY": 101.0, "minX": 118.0, "maxY": 118.0, "series": [{"data": [[382.0, 118.0], [118.0, 101.0], [500.0, 103.0], [501.0, 103.0], [499.0, 103.0], [502.0, 105.0], [498.0, 104.0]], "isOverall": false, "label": "Successes", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 502.0, "title": "Latencies Vs Request"}},
    getOptions: function() {
        return{
            series: {
                lines: {
                    show: false
                },
                points: {
                    show: true
                }
            },
            xaxis: {
                axisLabel: "Global number of requests per second",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            yaxis: {
                axisLabel: "Median Latency (ms)",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            legend: { noColumns: 2,show: true, container: '#legendLatencyVsRequest' },
            selection: {
                mode: 'xy'
            },
            grid: {
                hoverable: true // IMPORTANT! this is needed for tooltip to work
            },
            tooltip: true,
            tooltipOpts: {
                content: "%s : Median response time at %x req/s was %y ms"
            }
        };
    },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesLatencyVsRequest"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotLatenciesVsRequest"), dataset, options);
        // setup overview
        $.plot($("#overviewLatenciesVsRequest"), dataset, prepareOverviewOptions(options));
    }
};

// Latencies vs Request
function refreshLatenciesVsRequest() {
        var infos = latenciesVsRequestInfos;
        prepareSeries(infos.data);
        if(isGraph($("#flotLatenciesVsRequest"))){
            infos.createGraph();
        }else{
            var choiceContainer = $("#choicesLatencyVsRequest");
            createLegend(choiceContainer, infos);
            infos.createGraph();
            setGraphZoomable("#flotLatenciesVsRequest", "#overviewLatenciesVsRequest");
            $('#footerLatenciesVsRequest .legendColorBox > div').each(function(i){
                $(this).clone().prependTo(choiceContainer.find("li").eq(i));
            });
        }
};

var hitsPerSecondInfos = {
        data: {"result": {"minY": 1.95, "minX": 1.50890946E12, "maxY": 8.366666666666667, "series": [{"data": [[1.50891804E12, 8.333333333333334], [1.50891156E12, 8.333333333333334], [1.50891378E12, 8.333333333333334], [1.508916E12, 8.333333333333334], [1.50891702E12, 8.333333333333334], [1.50891036E12, 8.333333333333334], [1.5089148E12, 8.333333333333334], [1.50891258E12, 8.333333333333334], [1.50891582E12, 8.333333333333334], [1.50891174E12, 8.35], [1.50891618E12, 8.333333333333334], [1.50891822E12, 8.333333333333334], [1.50891072E12, 8.333333333333334], [1.50891396E12, 8.333333333333334], [1.50891054E12, 8.333333333333334], [1.50891276E12, 8.333333333333334], [1.50891498E12, 8.333333333333334], [1.5089172E12, 8.333333333333334], [1.50890952E12, 8.333333333333334], [1.5089184E12, 8.333333333333334], [1.50891666E12, 8.333333333333334], [1.50891222E12, 8.333333333333334], [1.5089187E12, 8.333333333333334], [1.5089112E12, 8.333333333333334], [1.50891444E12, 8.333333333333334], [1.50891768E12, 8.333333333333334], [1.50891102E12, 8.333333333333334], [1.50891324E12, 8.333333333333334], [1.50891546E12, 8.333333333333334], [1.50891888E12, 8.333333333333334], [1.50891E12, 8.333333333333334], [1.5089136E12, 8.333333333333334], [1.50891138E12, 8.333333333333334], [1.50891462E12, 8.333333333333334], [1.50891684E12, 8.333333333333334], [1.50891786E12, 8.333333333333334], [1.50891342E12, 8.333333333333334], [1.5089124E12, 8.333333333333334], [1.50891906E12, 1.95], [1.50891018E12, 8.333333333333334], [1.50891564E12, 8.333333333333334], [1.5089157E12, 8.333333333333334], [1.50891126E12, 8.333333333333334], [1.50891774E12, 8.333333333333334], [1.50891024E12, 8.333333333333334], [1.50891348E12, 8.333333333333334], [1.5089145E12, 8.333333333333334], [1.50891672E12, 8.333333333333334], [1.50891894E12, 8.333333333333334], [1.50891006E12, 8.333333333333334], [1.50891228E12, 8.333333333333334], [1.50891792E12, 8.333333333333334], [1.50891042E12, 8.333333333333334], [1.50891264E12, 8.333333333333334], [1.50891366E12, 8.333333333333334], [1.50891588E12, 8.333333333333334], [1.50891246E12, 8.333333333333334], [1.5089169E12, 8.333333333333334], [1.50891468E12, 8.333333333333334], [1.50891144E12, 8.333333333333334], [1.5089181E12, 8.333333333333334], [1.5089109E12, 8.333333333333334], [1.50891312E12, 8.333333333333334], [1.50891414E12, 8.333333333333334], [1.50891636E12, 8.333333333333334], [1.50891294E12, 8.3], [1.50891738E12, 8.333333333333334], [1.50891516E12, 8.333333333333334], [1.5089097E12, 8.333333333333334], [1.50891192E12, 8.333333333333334], [1.50891858E12, 8.333333333333334], [1.50891108E12, 8.333333333333334], [1.50891552E12, 8.333333333333334], [1.5089133E12, 8.333333333333334], [1.50891654E12, 8.333333333333334], [1.50891876E12, 8.333333333333334], [1.50890988E12, 8.333333333333334], [1.50891432E12, 8.333333333333334], [1.5089121E12, 8.333333333333334], [1.50891534E12, 8.333333333333334], [1.50891756E12, 8.333333333333334], [1.50891318E12, 8.333333333333334], [1.50891762E12, 8.333333333333334], [1.5089154E12, 8.333333333333334], [1.50891864E12, 8.333333333333334], [1.50890994E12, 8.333333333333334], [1.50891216E12, 8.333333333333334], [1.50891198E12, 8.366666666666667], [1.50891642E12, 8.333333333333334], [1.50891096E12, 8.333333333333334], [1.5089142E12, 8.333333333333334], [1.50891456E12, 8.333333333333334], [1.50891012E12, 8.333333333333334], [1.50891234E12, 8.333333333333334], [1.50891558E12, 8.333333333333334], [1.50891882E12, 8.333333333333334], [1.5089178E12, 8.333333333333334], [1.50891114E12, 8.333333333333334], [1.50891336E12, 8.333333333333334], [1.5089166E12, 8.333333333333334], [1.50891438E12, 8.333333333333334], [1.5089106E12, 8.333333333333334], [1.50891504E12, 8.333333333333334], [1.50891282E12, 8.333333333333334], [1.50891606E12, 8.333333333333334], [1.50891384E12, 8.333333333333334], [1.50891828E12, 8.333333333333334], [1.50891162E12, 8.333333333333334], [1.50891486E12, 8.333333333333334], [1.50891708E12, 8.333333333333334], [1.50891078E12, 8.333333333333334], [1.508913E12, 8.333333333333334], [1.50891522E12, 8.333333333333334], [1.50891744E12, 8.333333333333334], [1.50890976E12, 8.333333333333334], [1.50890958E12, 8.333333333333334], [1.5089118E12, 8.316666666666666], [1.50891402E12, 8.333333333333334], [1.50891624E12, 8.333333333333334], [1.50891846E12, 8.333333333333334], [1.50891726E12, 8.333333333333334], [1.50890964E12, 8.333333333333334], [1.50891408E12, 8.333333333333334], [1.50891186E12, 8.333333333333334], [1.5089151E12, 8.333333333333334], [1.50891732E12, 8.333333333333334], [1.50891834E12, 8.333333333333334], [1.50891066E12, 8.333333333333334], [1.50891288E12, 8.366666666666667], [1.5089139E12, 8.333333333333334], [1.50891612E12, 8.333333333333334], [1.50891852E12, 8.333333333333334], [1.50890982E12, 8.333333333333334], [1.50891204E12, 8.3], [1.50891426E12, 8.333333333333334], [1.50891648E12, 8.333333333333334], [1.5089175E12, 8.333333333333334], [1.50891084E12, 8.333333333333334], [1.50891528E12, 8.333333333333334], [1.50891306E12, 8.333333333333334], [1.5089163E12, 8.333333333333334], [1.508919E12, 8.333333333333334], [1.50891252E12, 8.333333333333334], [1.50891474E12, 8.333333333333334], [1.50891696E12, 8.333333333333334], [1.5089103E12, 8.333333333333334], [1.50891132E12, 8.333333333333334], [1.50891354E12, 8.333333333333334], [1.50891576E12, 8.333333333333334], [1.50891798E12, 8.333333333333334], [1.50891678E12, 8.333333333333334], [1.5089127E12, 8.333333333333334], [1.50891714E12, 8.333333333333334], [1.50891492E12, 8.333333333333334], [1.50891816E12, 8.333333333333334], [1.50890946E12, 6.383333333333334], [1.50891168E12, 8.333333333333334], [1.5089115E12, 8.333333333333334], [1.50891594E12, 8.333333333333334], [1.50891048E12, 8.333333333333334], [1.50891372E12, 8.333333333333334]], "isOverall": false, "label": "hitsPerSecond", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.50891906E12, "title": "Hits Per Second"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: "%H:%M:%S",
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of hits / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendHitsPerSecond"
                },
                selection: {
                    mode : 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y.2 hits/sec"
                }
            };
        },
        createGraph: function createGraph() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesHitsPerSecond"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotHitsPerSecond"), dataset, options);
            // setup overview
            $.plot($("#overviewHitsPerSecond"), dataset, prepareOverviewOptions(options));
        }
};

// Hits per second
function refreshHitsPerSecond(fixTimestamps) {
    var infos = hitsPerSecondInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -18000000);
    }
    if (isGraph($("#flotHitsPerSecond"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesHitsPerSecond");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotHitsPerSecond", "#overviewHitsPerSecond");
        $('#footerHitsPerSecond .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
}

var codesPerSecondInfos = {
        data: {"result": {"minY": 1.9666666666666666, "minX": 1.50890946E12, "maxY": 8.366666666666667, "series": [{"data": [[1.50891804E12, 8.316666666666666], [1.50891156E12, 8.333333333333334], [1.50891378E12, 8.333333333333334], [1.508916E12, 8.333333333333334], [1.50891702E12, 8.333333333333334], [1.50891036E12, 8.333333333333334], [1.5089148E12, 8.333333333333334], [1.50891258E12, 8.35], [1.50891582E12, 8.35], [1.50891174E12, 8.316666666666666], [1.50891618E12, 8.333333333333334], [1.50891822E12, 8.333333333333334], [1.50891072E12, 8.333333333333334], [1.50891396E12, 8.333333333333334], [1.50891054E12, 8.333333333333334], [1.50891276E12, 8.333333333333334], [1.50891498E12, 8.333333333333334], [1.5089172E12, 8.333333333333334], [1.50890952E12, 8.333333333333334], [1.5089184E12, 8.333333333333334], [1.50891666E12, 8.333333333333334], [1.50891222E12, 8.333333333333334], [1.5089187E12, 8.333333333333334], [1.5089112E12, 8.316666666666666], [1.50891444E12, 8.333333333333334], [1.50891768E12, 8.333333333333334], [1.50891102E12, 8.333333333333334], [1.50891324E12, 8.333333333333334], [1.50891546E12, 8.333333333333334], [1.50891888E12, 8.333333333333334], [1.50891E12, 8.333333333333334], [1.5089136E12, 8.333333333333334], [1.50891138E12, 8.333333333333334], [1.50891462E12, 8.35], [1.50891684E12, 8.333333333333334], [1.50891786E12, 8.35], [1.50891342E12, 8.333333333333334], [1.5089124E12, 8.333333333333334], [1.50891906E12, 1.9666666666666666], [1.50891018E12, 8.35], [1.50891564E12, 8.333333333333334], [1.5089157E12, 8.333333333333334], [1.50891126E12, 8.333333333333334], [1.50891774E12, 8.333333333333334], [1.50891024E12, 8.316666666666666], [1.50891348E12, 8.333333333333334], [1.5089145E12, 8.333333333333334], [1.50891672E12, 8.333333333333334], [1.50891894E12, 8.333333333333334], [1.50891006E12, 8.333333333333334], [1.50891228E12, 8.333333333333334], [1.50891792E12, 8.316666666666666], [1.50891042E12, 8.333333333333334], [1.50891264E12, 8.316666666666666], [1.50891366E12, 8.333333333333334], [1.50891588E12, 8.316666666666666], [1.50891246E12, 8.333333333333334], [1.5089169E12, 8.333333333333334], [1.50891468E12, 8.316666666666666], [1.50891144E12, 8.333333333333334], [1.5089181E12, 8.333333333333334], [1.5089109E12, 8.333333333333334], [1.50891312E12, 8.333333333333334], [1.50891414E12, 8.316666666666666], [1.50891636E12, 8.333333333333334], [1.50891294E12, 8.3], [1.50891738E12, 8.333333333333334], [1.50891516E12, 8.333333333333334], [1.5089097E12, 8.333333333333334], [1.50891192E12, 8.333333333333334], [1.50891858E12, 8.333333333333334], [1.50891108E12, 8.333333333333334], [1.50891552E12, 8.333333333333334], [1.5089133E12, 8.333333333333334], [1.50891654E12, 8.333333333333334], [1.50891876E12, 8.333333333333334], [1.50890988E12, 8.333333333333334], [1.50891432E12, 8.333333333333334], [1.5089121E12, 8.333333333333334], [1.50891534E12, 8.316666666666666], [1.50891756E12, 8.333333333333334], [1.50891318E12, 8.316666666666666], [1.50891762E12, 8.333333333333334], [1.5089154E12, 8.333333333333334], [1.50891864E12, 8.333333333333334], [1.50890994E12, 8.333333333333334], [1.50891216E12, 8.333333333333334], [1.50891198E12, 8.366666666666667], [1.50891642E12, 8.333333333333334], [1.50891096E12, 8.333333333333334], [1.5089142E12, 8.333333333333334], [1.50891456E12, 8.333333333333334], [1.50891012E12, 8.333333333333334], [1.50891234E12, 8.333333333333334], [1.50891558E12, 8.333333333333334], [1.50891882E12, 8.333333333333334], [1.5089178E12, 8.333333333333334], [1.50891114E12, 8.35], [1.50891336E12, 8.333333333333334], [1.5089166E12, 8.333333333333334], [1.50891438E12, 8.333333333333334], [1.5089106E12, 8.333333333333334], [1.50891504E12, 8.333333333333334], [1.50891282E12, 8.35], [1.50891606E12, 8.35], [1.50891384E12, 8.333333333333334], [1.50891828E12, 8.333333333333334], [1.50891162E12, 8.333333333333334], [1.50891486E12, 8.333333333333334], [1.50891708E12, 8.333333333333334], [1.50891078E12, 8.333333333333334], [1.508913E12, 8.35], [1.50891522E12, 8.333333333333334], [1.50891744E12, 8.333333333333334], [1.50890976E12, 8.333333333333334], [1.50890958E12, 8.333333333333334], [1.5089118E12, 8.333333333333334], [1.50891402E12, 8.333333333333334], [1.50891624E12, 8.333333333333334], [1.50891846E12, 8.333333333333334], [1.50891726E12, 8.333333333333334], [1.50890964E12, 8.333333333333334], [1.50891408E12, 8.35], [1.50891186E12, 8.333333333333334], [1.5089151E12, 8.333333333333334], [1.50891732E12, 8.333333333333334], [1.50891834E12, 8.333333333333334], [1.50891066E12, 8.333333333333334], [1.50891288E12, 8.35], [1.5089139E12, 8.333333333333334], [1.50891612E12, 8.316666666666666], [1.50891852E12, 8.333333333333334], [1.50890982E12, 8.333333333333334], [1.50891204E12, 8.3], [1.50891426E12, 8.333333333333334], [1.50891648E12, 8.333333333333334], [1.5089175E12, 8.333333333333334], [1.50891084E12, 8.333333333333334], [1.50891528E12, 8.35], [1.50891306E12, 8.333333333333334], [1.5089163E12, 8.333333333333334], [1.508919E12, 8.333333333333334], [1.50891252E12, 8.333333333333334], [1.50891474E12, 8.333333333333334], [1.50891696E12, 8.333333333333334], [1.5089103E12, 8.333333333333334], [1.50891132E12, 8.333333333333334], [1.50891354E12, 8.333333333333334], [1.50891576E12, 8.333333333333334], [1.50891798E12, 8.35], [1.50891678E12, 8.333333333333334], [1.5089127E12, 8.333333333333334], [1.50891714E12, 8.333333333333334], [1.50891492E12, 8.333333333333334], [1.50891816E12, 8.333333333333334], [1.50890946E12, 6.366666666666666], [1.50891168E12, 8.35], [1.5089115E12, 8.333333333333334], [1.50891594E12, 8.333333333333334], [1.50891048E12, 8.333333333333334], [1.50891372E12, 8.333333333333334]], "isOverall": false, "label": "200", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.50891906E12, "title": "Codes Per Second"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: "%H:%M:%S",
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of responses/sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendCodesPerSecond"
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "Number of Response Codes %s at %x was %y.2 responses / sec"
                }
            };
        },
    createGraph: function() {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesCodesPerSecond"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotCodesPerSecond"), dataset, options);
        // setup overview
        $.plot($("#overviewCodesPerSecond"), dataset, prepareOverviewOptions(options));
    }
};

// Codes per second
function refreshCodesPerSecond(fixTimestamps) {
    var infos = codesPerSecondInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -18000000);
    }
    if(isGraph($("#flotCodesPerSecond"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesCodesPerSecond");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotCodesPerSecond", "#overviewCodesPerSecond");
        $('#footerCodesPerSecond .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var transactionsPerSecondInfos = {
        data: {"result": {"minY": 1.9666666666666666, "minX": 1.50890946E12, "maxY": 8.366666666666667, "series": [{"data": [[1.50891804E12, 8.316666666666666], [1.50891156E12, 8.333333333333334], [1.50891378E12, 8.333333333333334], [1.508916E12, 8.333333333333334], [1.50891702E12, 8.333333333333334], [1.50891036E12, 8.333333333333334], [1.5089148E12, 8.333333333333334], [1.50891258E12, 8.35], [1.50891582E12, 8.35], [1.50891174E12, 8.316666666666666], [1.50891618E12, 8.333333333333334], [1.50891822E12, 8.333333333333334], [1.50891072E12, 8.333333333333334], [1.50891396E12, 8.333333333333334], [1.50891054E12, 8.333333333333334], [1.50891276E12, 8.333333333333334], [1.50891498E12, 8.333333333333334], [1.5089172E12, 8.333333333333334], [1.50890952E12, 8.333333333333334], [1.5089184E12, 8.333333333333334], [1.50891666E12, 8.333333333333334], [1.50891222E12, 8.333333333333334], [1.5089187E12, 8.333333333333334], [1.5089112E12, 8.316666666666666], [1.50891444E12, 8.333333333333334], [1.50891768E12, 8.333333333333334], [1.50891102E12, 8.333333333333334], [1.50891324E12, 8.333333333333334], [1.50891546E12, 8.333333333333334], [1.50891888E12, 8.333333333333334], [1.50891E12, 8.333333333333334], [1.5089136E12, 8.333333333333334], [1.50891138E12, 8.333333333333334], [1.50891462E12, 8.35], [1.50891684E12, 8.333333333333334], [1.50891786E12, 8.35], [1.50891342E12, 8.333333333333334], [1.5089124E12, 8.333333333333334], [1.50891906E12, 1.9666666666666666], [1.50891018E12, 8.35], [1.50891564E12, 8.333333333333334], [1.5089157E12, 8.333333333333334], [1.50891126E12, 8.333333333333334], [1.50891774E12, 8.333333333333334], [1.50891024E12, 8.316666666666666], [1.50891348E12, 8.333333333333334], [1.5089145E12, 8.333333333333334], [1.50891672E12, 8.333333333333334], [1.50891894E12, 8.333333333333334], [1.50891006E12, 8.333333333333334], [1.50891228E12, 8.333333333333334], [1.50891792E12, 8.316666666666666], [1.50891042E12, 8.333333333333334], [1.50891264E12, 8.316666666666666], [1.50891366E12, 8.333333333333334], [1.50891588E12, 8.316666666666666], [1.50891246E12, 8.333333333333334], [1.5089169E12, 8.333333333333334], [1.50891468E12, 8.316666666666666], [1.50891144E12, 8.333333333333334], [1.5089181E12, 8.333333333333334], [1.5089109E12, 8.333333333333334], [1.50891312E12, 8.333333333333334], [1.50891414E12, 8.316666666666666], [1.50891636E12, 8.333333333333334], [1.50891294E12, 8.3], [1.50891738E12, 8.333333333333334], [1.50891516E12, 8.333333333333334], [1.5089097E12, 8.333333333333334], [1.50891192E12, 8.333333333333334], [1.50891858E12, 8.333333333333334], [1.50891108E12, 8.333333333333334], [1.50891552E12, 8.333333333333334], [1.5089133E12, 8.333333333333334], [1.50891654E12, 8.333333333333334], [1.50891876E12, 8.333333333333334], [1.50890988E12, 8.333333333333334], [1.50891432E12, 8.333333333333334], [1.5089121E12, 8.333333333333334], [1.50891534E12, 8.316666666666666], [1.50891756E12, 8.333333333333334], [1.50891318E12, 8.316666666666666], [1.50891762E12, 8.333333333333334], [1.5089154E12, 8.333333333333334], [1.50891864E12, 8.333333333333334], [1.50890994E12, 8.333333333333334], [1.50891216E12, 8.333333333333334], [1.50891198E12, 8.366666666666667], [1.50891642E12, 8.333333333333334], [1.50891096E12, 8.333333333333334], [1.5089142E12, 8.333333333333334], [1.50891456E12, 8.333333333333334], [1.50891012E12, 8.333333333333334], [1.50891234E12, 8.333333333333334], [1.50891558E12, 8.333333333333334], [1.50891882E12, 8.333333333333334], [1.5089178E12, 8.333333333333334], [1.50891114E12, 8.35], [1.50891336E12, 8.333333333333334], [1.5089166E12, 8.333333333333334], [1.50891438E12, 8.333333333333334], [1.5089106E12, 8.333333333333334], [1.50891504E12, 8.333333333333334], [1.50891282E12, 8.35], [1.50891606E12, 8.35], [1.50891384E12, 8.333333333333334], [1.50891828E12, 8.333333333333334], [1.50891162E12, 8.333333333333334], [1.50891486E12, 8.333333333333334], [1.50891708E12, 8.333333333333334], [1.50891078E12, 8.333333333333334], [1.508913E12, 8.35], [1.50891522E12, 8.333333333333334], [1.50891744E12, 8.333333333333334], [1.50890976E12, 8.333333333333334], [1.50890958E12, 8.333333333333334], [1.5089118E12, 8.333333333333334], [1.50891402E12, 8.333333333333334], [1.50891624E12, 8.333333333333334], [1.50891846E12, 8.333333333333334], [1.50891726E12, 8.333333333333334], [1.50890964E12, 8.333333333333334], [1.50891408E12, 8.35], [1.50891186E12, 8.333333333333334], [1.5089151E12, 8.333333333333334], [1.50891732E12, 8.333333333333334], [1.50891834E12, 8.333333333333334], [1.50891066E12, 8.333333333333334], [1.50891288E12, 8.35], [1.5089139E12, 8.333333333333334], [1.50891612E12, 8.316666666666666], [1.50891852E12, 8.333333333333334], [1.50890982E12, 8.333333333333334], [1.50891204E12, 8.3], [1.50891426E12, 8.333333333333334], [1.50891648E12, 8.333333333333334], [1.5089175E12, 8.333333333333334], [1.50891084E12, 8.333333333333334], [1.50891528E12, 8.35], [1.50891306E12, 8.333333333333334], [1.5089163E12, 8.333333333333334], [1.508919E12, 8.333333333333334], [1.50891252E12, 8.333333333333334], [1.50891474E12, 8.333333333333334], [1.50891696E12, 8.333333333333334], [1.5089103E12, 8.333333333333334], [1.50891132E12, 8.333333333333334], [1.50891354E12, 8.333333333333334], [1.50891576E12, 8.333333333333334], [1.50891798E12, 8.35], [1.50891678E12, 8.333333333333334], [1.5089127E12, 8.333333333333334], [1.50891714E12, 8.333333333333334], [1.50891492E12, 8.333333333333334], [1.50891816E12, 8.333333333333334], [1.50890946E12, 6.366666666666666], [1.50891168E12, 8.35], [1.5089115E12, 8.333333333333334], [1.50891594E12, 8.333333333333334], [1.50891048E12, 8.333333333333334], [1.50891372E12, 8.333333333333334]], "isOverall": false, "label": "Process transactional inline data requests-success", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.50891906E12, "title": "Transactions Per Second"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: "%H:%M:%S",
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of transactions / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendTransactionsPerSecond"
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y transactions / sec"
                }
            };
        },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesTransactionsPerSecond"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotTransactionsPerSecond"), dataset, options);
        // setup overview
        $.plot($("#overviewTransactionsPerSecond"), dataset, prepareOverviewOptions(options));
    }
};

// Transactions per second
function refreshTransactionsPerSecond(fixTimestamps) {
    var infos = transactionsPerSecondInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -18000000);
    }
    if(isGraph($("#flotTransactionsPerSecond"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesTransactionsPerSecond");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotTransactionsPerSecond", "#overviewTransactionsPerSecond");
        $('#footerTransactionsPerSecond .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

// Collapse the graph matching the specified DOM element depending the collapsed
// status
function collapse(elem, collapsed){
    if(collapsed){
        $(elem).parent().find(".fa-chevron-up").removeClass("fa-chevron-up").addClass("fa-chevron-down");
    } else {
        $(elem).parent().find(".fa-chevron-down").removeClass("fa-chevron-down").addClass("fa-chevron-up");
        if (elem.id == "bodyBytesThroughputOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshBytesThroughputOverTime(true);
            }
            document.location.href="#responseTimesOverTime";
        } else if (elem.id == "bodyLantenciesOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshLatenciesOverTime(true);
            }
            document.location.href="#latenciesOverTime";
        } else if (elem.id == "bodyResponseTimeDistribution") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshResponseTimeDistribution();
            }
            document.location.href="#responseTimeDistribution" ;
        } else if (elem.id == "bodyActiveThreadsOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshActiveThreadsOverTime(true);
            }
            document.location.href="#activeThreadsOverTime";
        } else if (elem.id == "bodyTimeVsThreads") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshTimeVsThreads();
            }
            document.location.href="#timeVsThreads" ;
        } else if (elem.id == "bodyCodesPerSecond") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshCodesPerSecond(true);
            }
            document.location.href="#codesPerSecond";
        } else if (elem.id == "bodyTransactionsPerSecond") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshTransactionsPerSecond(true);
            }
            document.location.href="#transactionsPerSecond";
        } else if (elem.id == "bodyResponseTimeVsRequest") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshResponseTimeVsRequest();
            }
            document.location.href="#responseTimeVsRequest";
        } else if (elem.id == "bodyLatenciesVsRequest") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshLatenciesVsRequest();
            }
            document.location.href="#latencyVsRequest";
        }
    }
}

// Collapse
$(function() {
        $('.collapse').on('shown.bs.collapse', function(){
            collapse(this, false);
        }).on('hidden.bs.collapse', function(){
            collapse(this, true);
        });
});

$(function() {
    $(".glyphicon").mousedown( function(event){
        var tmp = $('.in:not(ul)');
        tmp.parent().parent().parent().find(".fa-chevron-up").removeClass("fa-chevron-down").addClass("fa-chevron-down");
        tmp.removeClass("in");
        tmp.addClass("out");
    });
});

/*
 * Activates or deactivates all series of the specified graph (represented by id parameter)
 * depending on checked argument.
 */
function toggleAll(id, checked){
    var placeholder = document.getElementById(id);

    var cases = $(placeholder).find(':checkbox');
    cases.prop('checked', checked);
    $(cases).parent().children().children().toggleClass("legend-disabled", !checked);

    var choiceContainer;
    if ( id == "choicesBytesThroughputOverTime"){
        choiceContainer = $("#choicesBytesThroughputOverTime");
        refreshBytesThroughputOverTime(false);
    } else if(id == "choicesResponseTimesOverTime"){
        choiceContainer = $("#choicesResponseTimesOverTime");
        refreshResponseTimeOverTime(false);
    } else if ( id == "choicesLatenciesOverTime"){
        choiceContainer = $("#choicesLatenciesOverTime");
        refreshLatenciesOverTime(false);
    } else if ( id == "choicesResponseTimePercentiles"){
        choiceContainer = $("#choicesResponseTimePercentiles");
        refreshResponseTimePercentiles();
    } else if(id == "choicesActiveThreadsOverTime"){
        choiceContainer = $("#choicesActiveThreadsOverTime");
        refreshActiveThreadsOverTime(false);
    } else if ( id == "choicesTimeVsThreads"){
        choiceContainer = $("#choicesTimeVsThreads");
        refreshTimeVsThreads();
    } else if ( id == "choicesResponseTimeDistribution"){
        choiceContainer = $("#choicesResponseTimeDistribution");
        refreshResponseTimeDistribution();
    } else if ( id == "choicesHitsPerSecond"){
        choiceContainer = $("#choicesHitsPerSecond");
        refreshHitsPerSecond(false);
    } else if(id == "choicesCodesPerSecond"){
        choiceContainer = $("#choicesCodesPerSecond");
        refreshCodesPerSecond(false);
    } else if ( id == "choicesTransactionsPerSecond"){
        choiceContainer = $("#choicesTransactionsPerSecond");
        refreshTransactionsPerSecond(false);
    } else if ( id == "choicesResponseTimeVsRequest"){
        choiceContainer = $("#choicesResponseTimeVsRequest");
        refreshResponseTimeVsRequest();
    } else if ( id == "choicesLatencyVsRequest"){
        choiceContainer = $("#choicesLatencyVsRequest");
        refreshLatenciesVsRequest();
    }
    var color = checked ? "black" : "#818181";
    choiceContainer.find("label").each(function(){
        this.style.color = color;
    });
}

// Unchecks all boxes for "Hide all samples" functionality
function uncheckAll(id){
    toggleAll(id, false);
}

// Checks all boxes for "Show all samples" functionality
function checkAll(id){
    toggleAll(id, true);
}

// Prepares data to be consumed by plot plugins
function prepareData(series, choiceContainer, customizeSeries){
    var datasets = [];

    // Add only selected series to the data set
    choiceContainer.find("input:checked").each(function (index, item) {
        var key = $(item).attr("name");
        var i = 0;
        var size = series.length;
        while(i < size && series[i].label != key)
            i++;
        if(i < size){
            var currentSeries = series[i];
            datasets.push(currentSeries);
            if(customizeSeries)
                customizeSeries(currentSeries);
        }
    });
    return datasets;
}

// create slider
$(function() {
    $( "#slider-vertical" ).slider({
      orientation: "vertical",
      range: "min",
      min: responseTimePercentilesInfos.data.result.minY,
      max: responseTimePercentilesInfos.data.result.maxY,
      value: 0,
      stop: function(event, ui ) {
       percentileThreshold= ui.value;
       refreshResponseTimePercentiles();
       $("#amount").val(percentileThreshold);
      }
    });
    $("#amount" ).val( $( "#slider-vertical" ).slider( "value" ) );
    $("#slider-vertical").children("div").css("background-color","purple");
    $("#amount" ).css("color", $("#slider-vertical").children("div").css("background-color"));
    $("#slider-vertical").children("div").css("opacity","0.3");
});

/*
 * Ignore case comparator
 */
function sortAlphaCaseless(a,b){
    return a.toLowerCase() > b.toLowerCase() ? 1 : -1;
};

/*
 * Creates a legend in the specified element with graph information
 */
function createLegend(choiceContainer, infos) {
    // Sort series by name
    var keys = [];
    $.each(infos.data.result.series, function(index, series){
        keys.push(series.label);
    });
    keys.sort(sortAlphaCaseless);

    // Create list of series with support of activation/deactivation
    $.each(keys, function(index, key) {
        var id = choiceContainer.attr('id') + index;
        $('<li />')
            .append($('<input id="' + id + '" name="' + key + '" type="checkbox" checked="checked" hidden />'))
            .append($('<label />', { 'text': key , 'for': id }))
            .appendTo(choiceContainer);
    });
    choiceContainer.find("label").click( function(){
        if (this.style.color !== "rgb(129, 129, 129)" ){
            this.style.color="#818181";
        }else {
            this.style.color="black";
        }
        $(this).parent().children().children().toggleClass("legend-disabled");
    });
    choiceContainer.find("label").mousedown( function(event){
        event.preventDefault();
    });
    choiceContainer.find("label").mouseenter(function(){
        this.style.cursor="pointer";
    });

    // Recreate graphe on series activation toggle
    choiceContainer.find("input").click(function(){
        infos.createGraph();
    });
}
