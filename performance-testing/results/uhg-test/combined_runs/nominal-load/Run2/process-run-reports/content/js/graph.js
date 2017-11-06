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
        data: {"result": {"minY": 117.0, "minX": 0.0, "maxY": 6990.0, "series": [{"data": [[0.0, 117.0], [0.1, 124.0], [0.2, 125.0], [0.3, 126.0], [0.4, 127.0], [0.5, 128.0], [0.6, 128.0], [0.7, 129.0], [0.8, 129.0], [0.9, 130.0], [1.0, 130.0], [1.1, 131.0], [1.2, 131.0], [1.3, 131.0], [1.4, 131.0], [1.5, 132.0], [1.6, 132.0], [1.7, 132.0], [1.8, 133.0], [1.9, 133.0], [2.0, 133.0], [2.1, 133.0], [2.2, 134.0], [2.3, 134.0], [2.4, 134.0], [2.5, 134.0], [2.6, 134.0], [2.7, 135.0], [2.8, 135.0], [2.9, 135.0], [3.0, 135.0], [3.1, 135.0], [3.2, 135.0], [3.3, 136.0], [3.4, 136.0], [3.5, 136.0], [3.6, 136.0], [3.7, 136.0], [3.8, 136.0], [3.9, 137.0], [4.0, 137.0], [4.1, 137.0], [4.2, 137.0], [4.3, 137.0], [4.4, 137.0], [4.5, 137.0], [4.6, 138.0], [4.7, 138.0], [4.8, 138.0], [4.9, 138.0], [5.0, 138.0], [5.1, 138.0], [5.2, 138.0], [5.3, 139.0], [5.4, 139.0], [5.5, 139.0], [5.6, 139.0], [5.7, 139.0], [5.8, 139.0], [5.9, 139.0], [6.0, 139.0], [6.1, 139.0], [6.2, 140.0], [6.3, 140.0], [6.4, 140.0], [6.5, 140.0], [6.6, 140.0], [6.7, 140.0], [6.8, 140.0], [6.9, 140.0], [7.0, 140.0], [7.1, 141.0], [7.2, 141.0], [7.3, 141.0], [7.4, 141.0], [7.5, 141.0], [7.6, 141.0], [7.7, 141.0], [7.8, 141.0], [7.9, 141.0], [8.0, 141.0], [8.1, 142.0], [8.2, 142.0], [8.3, 142.0], [8.4, 142.0], [8.5, 142.0], [8.6, 142.0], [8.7, 142.0], [8.8, 142.0], [8.9, 142.0], [9.0, 142.0], [9.1, 143.0], [9.2, 143.0], [9.3, 143.0], [9.4, 143.0], [9.5, 143.0], [9.6, 143.0], [9.7, 143.0], [9.8, 143.0], [9.9, 143.0], [10.0, 143.0], [10.1, 143.0], [10.2, 144.0], [10.3, 144.0], [10.4, 144.0], [10.5, 144.0], [10.6, 144.0], [10.7, 144.0], [10.8, 144.0], [10.9, 144.0], [11.0, 144.0], [11.1, 144.0], [11.2, 144.0], [11.3, 144.0], [11.4, 145.0], [11.5, 145.0], [11.6, 145.0], [11.7, 145.0], [11.8, 145.0], [11.9, 145.0], [12.0, 145.0], [12.1, 145.0], [12.2, 145.0], [12.3, 145.0], [12.4, 145.0], [12.5, 145.0], [12.6, 146.0], [12.7, 146.0], [12.8, 146.0], [12.9, 146.0], [13.0, 146.0], [13.1, 146.0], [13.2, 146.0], [13.3, 146.0], [13.4, 146.0], [13.5, 146.0], [13.6, 146.0], [13.7, 146.0], [13.8, 146.0], [13.9, 147.0], [14.0, 147.0], [14.1, 147.0], [14.2, 147.0], [14.3, 147.0], [14.4, 147.0], [14.5, 147.0], [14.6, 147.0], [14.7, 147.0], [14.8, 147.0], [14.9, 147.0], [15.0, 147.0], [15.1, 147.0], [15.2, 148.0], [15.3, 148.0], [15.4, 148.0], [15.5, 148.0], [15.6, 148.0], [15.7, 148.0], [15.8, 148.0], [15.9, 148.0], [16.0, 148.0], [16.1, 148.0], [16.2, 148.0], [16.3, 148.0], [16.4, 148.0], [16.5, 149.0], [16.6, 149.0], [16.7, 149.0], [16.8, 149.0], [16.9, 149.0], [17.0, 149.0], [17.1, 149.0], [17.2, 149.0], [17.3, 149.0], [17.4, 149.0], [17.5, 149.0], [17.6, 149.0], [17.7, 149.0], [17.8, 149.0], [17.9, 150.0], [18.0, 150.0], [18.1, 150.0], [18.2, 150.0], [18.3, 150.0], [18.4, 150.0], [18.5, 150.0], [18.6, 150.0], [18.7, 150.0], [18.8, 150.0], [18.9, 150.0], [19.0, 150.0], [19.1, 150.0], [19.2, 150.0], [19.3, 151.0], [19.4, 151.0], [19.5, 151.0], [19.6, 151.0], [19.7, 151.0], [19.8, 151.0], [19.9, 151.0], [20.0, 151.0], [20.1, 151.0], [20.2, 151.0], [20.3, 151.0], [20.4, 151.0], [20.5, 151.0], [20.6, 151.0], [20.7, 151.0], [20.8, 152.0], [20.9, 152.0], [21.0, 152.0], [21.1, 152.0], [21.2, 152.0], [21.3, 152.0], [21.4, 152.0], [21.5, 152.0], [21.6, 152.0], [21.7, 152.0], [21.8, 152.0], [21.9, 152.0], [22.0, 152.0], [22.1, 152.0], [22.2, 153.0], [22.3, 153.0], [22.4, 153.0], [22.5, 153.0], [22.6, 153.0], [22.7, 153.0], [22.8, 153.0], [22.9, 153.0], [23.0, 153.0], [23.1, 153.0], [23.2, 153.0], [23.3, 153.0], [23.4, 153.0], [23.5, 153.0], [23.6, 153.0], [23.7, 153.0], [23.8, 153.0], [23.9, 154.0], [24.0, 154.0], [24.1, 154.0], [24.2, 154.0], [24.3, 154.0], [24.4, 154.0], [24.5, 154.0], [24.6, 154.0], [24.7, 154.0], [24.8, 154.0], [24.9, 154.0], [25.0, 154.0], [25.1, 154.0], [25.2, 154.0], [25.3, 154.0], [25.4, 154.0], [25.5, 155.0], [25.6, 155.0], [25.7, 155.0], [25.8, 155.0], [25.9, 155.0], [26.0, 155.0], [26.1, 155.0], [26.2, 155.0], [26.3, 155.0], [26.4, 155.0], [26.5, 155.0], [26.6, 155.0], [26.7, 155.0], [26.8, 155.0], [26.9, 155.0], [27.0, 155.0], [27.1, 156.0], [27.2, 156.0], [27.3, 156.0], [27.4, 156.0], [27.5, 156.0], [27.6, 156.0], [27.7, 156.0], [27.8, 156.0], [27.9, 156.0], [28.0, 156.0], [28.1, 156.0], [28.2, 156.0], [28.3, 156.0], [28.4, 156.0], [28.5, 157.0], [28.6, 157.0], [28.7, 157.0], [28.8, 157.0], [28.9, 157.0], [29.0, 157.0], [29.1, 157.0], [29.2, 157.0], [29.3, 157.0], [29.4, 157.0], [29.5, 157.0], [29.6, 157.0], [29.7, 157.0], [29.8, 157.0], [29.9, 157.0], [30.0, 157.0], [30.1, 158.0], [30.2, 158.0], [30.3, 158.0], [30.4, 158.0], [30.5, 158.0], [30.6, 158.0], [30.7, 158.0], [30.8, 158.0], [30.9, 158.0], [31.0, 158.0], [31.1, 158.0], [31.2, 158.0], [31.3, 158.0], [31.4, 158.0], [31.5, 158.0], [31.6, 158.0], [31.7, 158.0], [31.8, 159.0], [31.9, 159.0], [32.0, 159.0], [32.1, 159.0], [32.2, 159.0], [32.3, 159.0], [32.4, 159.0], [32.5, 159.0], [32.6, 159.0], [32.7, 159.0], [32.8, 159.0], [32.9, 159.0], [33.0, 159.0], [33.1, 159.0], [33.2, 159.0], [33.3, 159.0], [33.4, 160.0], [33.5, 160.0], [33.6, 160.0], [33.7, 160.0], [33.8, 160.0], [33.9, 160.0], [34.0, 160.0], [34.1, 160.0], [34.2, 160.0], [34.3, 160.0], [34.4, 160.0], [34.5, 160.0], [34.6, 160.0], [34.7, 160.0], [34.8, 160.0], [34.9, 160.0], [35.0, 161.0], [35.1, 161.0], [35.2, 161.0], [35.3, 161.0], [35.4, 161.0], [35.5, 161.0], [35.6, 161.0], [35.7, 161.0], [35.8, 161.0], [35.9, 161.0], [36.0, 161.0], [36.1, 161.0], [36.2, 161.0], [36.3, 161.0], [36.4, 161.0], [36.5, 161.0], [36.6, 162.0], [36.7, 162.0], [36.8, 162.0], [36.9, 162.0], [37.0, 162.0], [37.1, 162.0], [37.2, 162.0], [37.3, 162.0], [37.4, 162.0], [37.5, 162.0], [37.6, 162.0], [37.7, 162.0], [37.8, 162.0], [37.9, 162.0], [38.0, 162.0], [38.1, 163.0], [38.2, 163.0], [38.3, 163.0], [38.4, 163.0], [38.5, 163.0], [38.6, 163.0], [38.7, 163.0], [38.8, 163.0], [38.9, 163.0], [39.0, 163.0], [39.1, 163.0], [39.2, 163.0], [39.3, 163.0], [39.4, 163.0], [39.5, 163.0], [39.6, 163.0], [39.7, 164.0], [39.8, 164.0], [39.9, 164.0], [40.0, 164.0], [40.1, 164.0], [40.2, 164.0], [40.3, 164.0], [40.4, 164.0], [40.5, 164.0], [40.6, 164.0], [40.7, 164.0], [40.8, 164.0], [40.9, 164.0], [41.0, 164.0], [41.1, 164.0], [41.2, 164.0], [41.3, 165.0], [41.4, 165.0], [41.5, 165.0], [41.6, 165.0], [41.7, 165.0], [41.8, 165.0], [41.9, 165.0], [42.0, 165.0], [42.1, 165.0], [42.2, 165.0], [42.3, 165.0], [42.4, 165.0], [42.5, 165.0], [42.6, 165.0], [42.7, 165.0], [42.8, 165.0], [42.9, 166.0], [43.0, 166.0], [43.1, 166.0], [43.2, 166.0], [43.3, 166.0], [43.4, 166.0], [43.5, 166.0], [43.6, 166.0], [43.7, 166.0], [43.8, 166.0], [43.9, 166.0], [44.0, 166.0], [44.1, 166.0], [44.2, 166.0], [44.3, 166.0], [44.4, 167.0], [44.5, 167.0], [44.6, 167.0], [44.7, 167.0], [44.8, 167.0], [44.9, 167.0], [45.0, 167.0], [45.1, 167.0], [45.2, 167.0], [45.3, 167.0], [45.4, 167.0], [45.5, 167.0], [45.6, 167.0], [45.7, 167.0], [45.8, 167.0], [45.9, 167.0], [46.0, 168.0], [46.1, 168.0], [46.2, 168.0], [46.3, 168.0], [46.4, 168.0], [46.5, 168.0], [46.6, 168.0], [46.7, 168.0], [46.8, 168.0], [46.9, 168.0], [47.0, 168.0], [47.1, 168.0], [47.2, 168.0], [47.3, 168.0], [47.4, 169.0], [47.5, 169.0], [47.6, 169.0], [47.7, 169.0], [47.8, 169.0], [47.9, 169.0], [48.0, 169.0], [48.1, 169.0], [48.2, 169.0], [48.3, 169.0], [48.4, 169.0], [48.5, 169.0], [48.6, 169.0], [48.7, 169.0], [48.8, 169.0], [48.9, 170.0], [49.0, 170.0], [49.1, 170.0], [49.2, 170.0], [49.3, 170.0], [49.4, 170.0], [49.5, 170.0], [49.6, 170.0], [49.7, 170.0], [49.8, 170.0], [49.9, 170.0], [50.0, 170.0], [50.1, 170.0], [50.2, 170.0], [50.3, 170.0], [50.4, 171.0], [50.5, 171.0], [50.6, 171.0], [50.7, 171.0], [50.8, 171.0], [50.9, 171.0], [51.0, 171.0], [51.1, 171.0], [51.2, 171.0], [51.3, 171.0], [51.4, 171.0], [51.5, 171.0], [51.6, 171.0], [51.7, 172.0], [51.8, 172.0], [51.9, 172.0], [52.0, 172.0], [52.1, 172.0], [52.2, 172.0], [52.3, 172.0], [52.4, 172.0], [52.5, 172.0], [52.6, 172.0], [52.7, 172.0], [52.8, 172.0], [52.9, 172.0], [53.0, 172.0], [53.1, 173.0], [53.2, 173.0], [53.3, 173.0], [53.4, 173.0], [53.5, 173.0], [53.6, 173.0], [53.7, 173.0], [53.8, 173.0], [53.9, 173.0], [54.0, 173.0], [54.1, 173.0], [54.2, 173.0], [54.3, 173.0], [54.4, 173.0], [54.5, 174.0], [54.6, 174.0], [54.7, 174.0], [54.8, 174.0], [54.9, 174.0], [55.0, 174.0], [55.1, 174.0], [55.2, 174.0], [55.3, 174.0], [55.4, 174.0], [55.5, 174.0], [55.6, 174.0], [55.7, 174.0], [55.8, 174.0], [55.9, 175.0], [56.0, 175.0], [56.1, 175.0], [56.2, 175.0], [56.3, 175.0], [56.4, 175.0], [56.5, 175.0], [56.6, 175.0], [56.7, 175.0], [56.8, 175.0], [56.9, 175.0], [57.0, 175.0], [57.1, 175.0], [57.2, 176.0], [57.3, 176.0], [57.4, 176.0], [57.5, 176.0], [57.6, 176.0], [57.7, 176.0], [57.8, 176.0], [57.9, 176.0], [58.0, 176.0], [58.1, 176.0], [58.2, 176.0], [58.3, 176.0], [58.4, 176.0], [58.5, 177.0], [58.6, 177.0], [58.7, 177.0], [58.8, 177.0], [58.9, 177.0], [59.0, 177.0], [59.1, 177.0], [59.2, 177.0], [59.3, 177.0], [59.4, 177.0], [59.5, 177.0], [59.6, 178.0], [59.7, 178.0], [59.8, 178.0], [59.9, 178.0], [60.0, 178.0], [60.1, 178.0], [60.2, 178.0], [60.3, 178.0], [60.4, 178.0], [60.5, 178.0], [60.6, 178.0], [60.7, 178.0], [60.8, 178.0], [60.9, 179.0], [61.0, 179.0], [61.1, 179.0], [61.2, 179.0], [61.3, 179.0], [61.4, 179.0], [61.5, 179.0], [61.6, 179.0], [61.7, 179.0], [61.8, 179.0], [61.9, 179.0], [62.0, 180.0], [62.1, 180.0], [62.2, 180.0], [62.3, 180.0], [62.4, 180.0], [62.5, 180.0], [62.6, 180.0], [62.7, 180.0], [62.8, 180.0], [62.9, 180.0], [63.0, 180.0], [63.1, 180.0], [63.2, 181.0], [63.3, 181.0], [63.4, 181.0], [63.5, 181.0], [63.6, 181.0], [63.7, 181.0], [63.8, 181.0], [63.9, 181.0], [64.0, 181.0], [64.1, 181.0], [64.2, 181.0], [64.3, 182.0], [64.4, 182.0], [64.5, 182.0], [64.6, 182.0], [64.7, 182.0], [64.8, 182.0], [64.9, 182.0], [65.0, 182.0], [65.1, 182.0], [65.2, 182.0], [65.3, 182.0], [65.4, 183.0], [65.5, 183.0], [65.6, 183.0], [65.7, 183.0], [65.8, 183.0], [65.9, 183.0], [66.0, 183.0], [66.1, 183.0], [66.2, 183.0], [66.3, 183.0], [66.4, 183.0], [66.5, 184.0], [66.6, 184.0], [66.7, 184.0], [66.8, 184.0], [66.9, 184.0], [67.0, 184.0], [67.1, 184.0], [67.2, 184.0], [67.3, 184.0], [67.4, 184.0], [67.5, 185.0], [67.6, 185.0], [67.7, 185.0], [67.8, 185.0], [67.9, 185.0], [68.0, 185.0], [68.1, 185.0], [68.2, 185.0], [68.3, 185.0], [68.4, 185.0], [68.5, 185.0], [68.6, 186.0], [68.7, 186.0], [68.8, 186.0], [68.9, 186.0], [69.0, 186.0], [69.1, 186.0], [69.2, 186.0], [69.3, 186.0], [69.4, 186.0], [69.5, 187.0], [69.6, 187.0], [69.7, 187.0], [69.8, 187.0], [69.9, 187.0], [70.0, 187.0], [70.1, 187.0], [70.2, 187.0], [70.3, 187.0], [70.4, 188.0], [70.5, 188.0], [70.6, 188.0], [70.7, 188.0], [70.8, 188.0], [70.9, 188.0], [71.0, 188.0], [71.1, 188.0], [71.2, 188.0], [71.3, 188.0], [71.4, 189.0], [71.5, 189.0], [71.6, 189.0], [71.7, 189.0], [71.8, 189.0], [71.9, 189.0], [72.0, 189.0], [72.1, 189.0], [72.2, 189.0], [72.3, 190.0], [72.4, 190.0], [72.5, 190.0], [72.6, 190.0], [72.7, 190.0], [72.8, 190.0], [72.9, 190.0], [73.0, 190.0], [73.1, 190.0], [73.2, 191.0], [73.3, 191.0], [73.4, 191.0], [73.5, 191.0], [73.6, 191.0], [73.7, 191.0], [73.8, 191.0], [73.9, 191.0], [74.0, 191.0], [74.1, 192.0], [74.2, 192.0], [74.3, 192.0], [74.4, 192.0], [74.5, 192.0], [74.6, 192.0], [74.7, 192.0], [74.8, 192.0], [74.9, 193.0], [75.0, 193.0], [75.1, 193.0], [75.2, 193.0], [75.3, 193.0], [75.4, 193.0], [75.5, 193.0], [75.6, 193.0], [75.7, 194.0], [75.8, 194.0], [75.9, 194.0], [76.0, 194.0], [76.1, 194.0], [76.2, 194.0], [76.3, 194.0], [76.4, 194.0], [76.5, 195.0], [76.6, 195.0], [76.7, 195.0], [76.8, 195.0], [76.9, 195.0], [77.0, 195.0], [77.1, 195.0], [77.2, 195.0], [77.3, 196.0], [77.4, 196.0], [77.5, 196.0], [77.6, 196.0], [77.7, 196.0], [77.8, 196.0], [77.9, 196.0], [78.0, 197.0], [78.1, 197.0], [78.2, 197.0], [78.3, 197.0], [78.4, 197.0], [78.5, 197.0], [78.6, 197.0], [78.7, 197.0], [78.8, 198.0], [78.9, 198.0], [79.0, 198.0], [79.1, 198.0], [79.2, 198.0], [79.3, 198.0], [79.4, 198.0], [79.5, 199.0], [79.6, 199.0], [79.7, 199.0], [79.8, 199.0], [79.9, 199.0], [80.0, 199.0], [80.1, 199.0], [80.2, 200.0], [80.3, 200.0], [80.4, 200.0], [80.5, 200.0], [80.6, 200.0], [80.7, 200.0], [80.8, 201.0], [80.9, 201.0], [81.0, 201.0], [81.1, 201.0], [81.2, 201.0], [81.3, 201.0], [81.4, 201.0], [81.5, 202.0], [81.6, 202.0], [81.7, 202.0], [81.8, 202.0], [81.9, 202.0], [82.0, 202.0], [82.1, 203.0], [82.2, 203.0], [82.3, 203.0], [82.4, 203.0], [82.5, 203.0], [82.6, 203.0], [82.7, 204.0], [82.8, 204.0], [82.9, 204.0], [83.0, 204.0], [83.1, 204.0], [83.2, 204.0], [83.3, 205.0], [83.4, 205.0], [83.5, 205.0], [83.6, 205.0], [83.7, 205.0], [83.8, 205.0], [83.9, 206.0], [84.0, 206.0], [84.1, 206.0], [84.2, 206.0], [84.3, 206.0], [84.4, 207.0], [84.5, 207.0], [84.6, 207.0], [84.7, 207.0], [84.8, 207.0], [84.9, 207.0], [85.0, 208.0], [85.1, 208.0], [85.2, 208.0], [85.3, 208.0], [85.4, 208.0], [85.5, 209.0], [85.6, 209.0], [85.7, 209.0], [85.8, 209.0], [85.9, 209.0], [86.0, 210.0], [86.1, 210.0], [86.2, 210.0], [86.3, 210.0], [86.4, 210.0], [86.5, 211.0], [86.6, 211.0], [86.7, 211.0], [86.8, 211.0], [86.9, 211.0], [87.0, 212.0], [87.1, 212.0], [87.2, 212.0], [87.3, 212.0], [87.4, 212.0], [87.5, 213.0], [87.6, 213.0], [87.7, 213.0], [87.8, 213.0], [87.9, 214.0], [88.0, 214.0], [88.1, 214.0], [88.2, 214.0], [88.3, 214.0], [88.4, 215.0], [88.5, 215.0], [88.6, 215.0], [88.7, 215.0], [88.8, 216.0], [88.9, 216.0], [89.0, 216.0], [89.1, 216.0], [89.2, 217.0], [89.3, 217.0], [89.4, 217.0], [89.5, 217.0], [89.6, 218.0], [89.7, 218.0], [89.8, 218.0], [89.9, 218.0], [90.0, 219.0], [90.1, 219.0], [90.2, 219.0], [90.3, 220.0], [90.4, 220.0], [90.5, 220.0], [90.6, 220.0], [90.7, 221.0], [90.8, 221.0], [90.9, 221.0], [91.0, 222.0], [91.1, 222.0], [91.2, 222.0], [91.3, 223.0], [91.4, 223.0], [91.5, 223.0], [91.6, 224.0], [91.7, 224.0], [91.8, 224.0], [91.9, 224.0], [92.0, 225.0], [92.1, 225.0], [92.2, 225.0], [92.3, 226.0], [92.4, 226.0], [92.5, 226.0], [92.6, 227.0], [92.7, 227.0], [92.8, 227.0], [92.9, 228.0], [93.0, 228.0], [93.1, 228.0], [93.2, 229.0], [93.3, 229.0], [93.4, 229.0], [93.5, 230.0], [93.6, 230.0], [93.7, 231.0], [93.8, 231.0], [93.9, 232.0], [94.0, 232.0], [94.1, 232.0], [94.2, 233.0], [94.3, 233.0], [94.4, 234.0], [94.5, 234.0], [94.6, 234.0], [94.7, 235.0], [94.8, 235.0], [94.9, 236.0], [95.0, 236.0], [95.1, 237.0], [95.2, 237.0], [95.3, 238.0], [95.4, 238.0], [95.5, 239.0], [95.6, 240.0], [95.7, 240.0], [95.8, 241.0], [95.9, 241.0], [96.0, 242.0], [96.1, 242.0], [96.2, 243.0], [96.3, 244.0], [96.4, 244.0], [96.5, 245.0], [96.6, 246.0], [96.7, 247.0], [96.8, 247.0], [96.9, 248.0], [97.0, 249.0], [97.1, 250.0], [97.2, 250.0], [97.3, 251.0], [97.4, 252.0], [97.5, 253.0], [97.6, 254.0], [97.7, 255.0], [97.8, 256.0], [97.9, 257.0], [98.0, 259.0], [98.1, 260.0], [98.2, 261.0], [98.3, 262.0], [98.4, 264.0], [98.5, 265.0], [98.6, 267.0], [98.7, 269.0], [98.8, 271.0], [98.9, 273.0], [99.0, 275.0], [99.1, 278.0], [99.2, 282.0], [99.3, 285.0], [99.4, 290.0], [99.5, 296.0], [99.6, 303.0], [99.7, 313.0], [99.8, 338.0], [99.9, 833.0]], "isOverall": false, "label": "Process transactional inline data requests", "isController": false}], "supportsControllersDiscrimination": true, "maxX": 100.0, "title": "Response Time Percentiles"}},
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
        data: {"result": {"minY": 1.0, "minX": 0.0, "maxY": 79893.0, "series": [{"data": [[0.0, 79893.0], [4500.0, 2.0], [2500.0, 1.0], [5000.0, 1.0], [1500.0, 4.0], [3000.0, 5.0], [6500.0, 1.0], [3500.0, 1.0], [500.0, 55.0], [1000.0, 34.0], [2000.0, 2.0], [4000.0, 1.0]], "isOverall": false, "label": "Process transactional inline data requests", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 500, "maxX": 6500.0, "title": "Response Time Distribution"}},
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
        data: {"result": {"minY": 47.13863636363639, "minX": 1.50969798E12, "maxY": 50.0, "series": [{"data": [[1.50970668E12, 50.0], [1.50969798E12, 50.0], [1.5097002E12, 50.0], [1.50970242E12, 50.0], [1.50970464E12, 50.0], [1.50970566E12, 50.0], [1.509699E12, 50.0], [1.50970344E12, 50.0], [1.50970122E12, 50.0], [1.50970446E12, 50.0], [1.50970224E12, 50.0], [1.50970002E12, 50.0], [1.50970326E12, 50.0], [1.50970548E12, 50.0], [1.5097065E12, 50.0], [1.50970104E12, 50.0], [1.50969882E12, 50.0], [1.50970206E12, 50.0], [1.50970428E12, 50.0], [1.50969954E12, 50.0], [1.50970176E12, 50.0], [1.50970278E12, 50.0], [1.509705E12, 50.0], [1.50970158E12, 50.0], [1.50970602E12, 50.0], [1.5097038E12, 50.0], [1.50970722E12, 50.0], [1.50969834E12, 50.0], [1.50970056E12, 50.0], [1.50970038E12, 50.0], [1.50970482E12, 50.0], [1.50970686E12, 50.0], [1.50969936E12, 50.0], [1.5097026E12, 50.0], [1.50969918E12, 50.0], [1.5097014E12, 50.0], [1.50970362E12, 50.0], [1.50970584E12, 50.0], [1.50969816E12, 50.0], [1.50970704E12, 50.0], [1.5096999E12, 50.0], [1.50970434E12, 50.0], [1.50970638E12, 50.0], [1.50969888E12, 50.0], [1.50970212E12, 50.0], [1.50970314E12, 50.0], [1.50970536E12, 50.0], [1.50970758E12, 47.13863636363639], [1.5096987E12, 50.0], [1.50970092E12, 50.0], [1.50970656E12, 50.0], [1.50969972E12, 50.0], [1.5097062E12, 50.0], [1.50970416E12, 50.0], [1.50970194E12, 50.0], [1.50970518E12, 50.0], [1.5097074E12, 50.0], [1.50969852E12, 50.0], [1.50970296E12, 50.0], [1.50970074E12, 50.0], [1.50970398E12, 50.0], [1.50969924E12, 50.0], [1.50970368E12, 50.0], [1.50970146E12, 50.0], [1.5097047E12, 50.0], [1.50969804E12, 50.0], [1.50970248E12, 50.0], [1.50970692E12, 50.0], [1.50970026E12, 50.0], [1.5097035E12, 50.0], [1.50970572E12, 50.0], [1.50969906E12, 50.0], [1.50970128E12, 50.0], [1.5097023E12, 50.0], [1.50970452E12, 50.0], [1.5097011E12, 50.0], [1.50970554E12, 50.0], [1.50970332E12, 50.0], [1.50970008E12, 50.0], [1.50970674E12, 50.0], [1.50970182E12, 50.0], [1.50970404E12, 50.0], [1.50970728E12, 50.0], [1.50969858E12, 50.0], [1.5097008E12, 50.0], [1.50970062E12, 50.0], [1.50970506E12, 50.0], [1.5096996E12, 50.0], [1.50970284E12, 50.0], [1.50970626E12, 50.0], [1.50969942E12, 50.0], [1.50970164E12, 50.0], [1.50970386E12, 50.0], [1.50970608E12, 50.0], [1.5096984E12, 50.0], [1.50969822E12, 50.0], [1.50970044E12, 50.0], [1.50970266E12, 50.0], [1.50970488E12, 50.0], [1.5097071E12, 50.0], [1.5097059E12, 50.0], [1.50969894E12, 50.0], [1.50970116E12, 50.0], [1.50970338E12, 50.0], [1.5097056E12, 50.0], [1.50969996E12, 50.0], [1.50970218E12, 50.0], [1.5097044E12, 50.0], [1.50970662E12, 50.0], [1.50970542E12, 50.0], [1.5097032E12, 50.0], [1.50969876E12, 50.0], [1.50970098E12, 50.0], [1.50970422E12, 50.0], [1.50970746E12, 50.0], [1.509702E12, 50.0], [1.50970644E12, 50.0], [1.50969978E12, 50.0], [1.50970524E12, 50.0], [1.50970302E12, 50.0], [1.50969828E12, 50.0], [1.50970272E12, 50.0], [1.5097005E12, 50.0], [1.50970374E12, 50.0], [1.50970596E12, 50.0], [1.50970698E12, 50.0], [1.5096993E12, 50.0], [1.50970152E12, 50.0], [1.50970254E12, 50.0], [1.50970476E12, 50.0], [1.50970134E12, 50.0], [1.50970578E12, 50.0], [1.50970356E12, 50.0], [1.5097068E12, 50.0], [1.5096981E12, 50.0], [1.50970032E12, 50.0], [1.50970014E12, 50.0], [1.50970458E12, 50.0], [1.50969912E12, 50.0], [1.50970236E12, 50.0], [1.5097053E12, 50.0], [1.50970086E12, 50.0], [1.50970734E12, 50.0], [1.50969984E12, 50.0], [1.50970308E12, 50.0], [1.50970632E12, 50.0], [1.5097041E12, 50.0], [1.50969966E12, 50.0], [1.50970188E12, 50.0], [1.50970752E12, 50.0], [1.50969864E12, 50.0], [1.50970716E12, 50.0], [1.50969846E12, 50.0], [1.50970068E12, 50.0], [1.5097029E12, 50.0], [1.50970512E12, 50.0], [1.50970614E12, 50.0], [1.50969948E12, 50.0], [1.50970392E12, 50.0], [1.5097017E12, 50.0], [1.50970494E12, 50.0]], "isOverall": false, "label": "ProcessRequestTransactionalInlineDataOnly", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.50970758E12, "title": "Active Threads Over Time"}},
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
        data: {"result": {"minY": 130.0, "minX": 1.0, "maxY": 204.0, "series": [{"data": [[2.0, 159.0], [3.0, 173.0], [4.0, 156.0], [5.0, 142.0], [6.0, 152.0], [7.0, 153.0], [8.0, 175.0], [9.0, 150.0], [10.0, 159.0], [11.0, 144.0], [12.0, 163.0], [13.0, 162.0], [14.0, 153.0], [15.0, 158.0], [16.0, 155.0], [17.0, 155.0], [18.0, 175.0], [19.0, 176.0], [20.0, 147.0], [21.0, 201.0], [22.0, 204.0], [23.0, 188.0], [24.0, 153.0], [25.0, 163.0], [26.0, 138.0], [27.0, 185.0], [28.0, 143.0], [29.0, 204.0], [30.0, 170.0], [31.0, 159.0], [33.0, 183.0], [32.0, 168.0], [35.0, 168.0], [34.0, 143.0], [37.0, 164.0], [36.0, 184.0], [39.0, 155.5], [38.0, 168.0], [41.0, 130.0], [40.0, 167.0], [43.0, 153.0], [42.0, 155.0], [45.0, 140.0], [44.0, 163.5], [47.0, 150.5], [46.0, 159.0], [49.0, 165.0], [48.0, 154.0], [50.0, 178.10264718392892], [1.0, 142.0]], "isOverall": false, "label": "Process transactional inline data requests", "isController": false}, {"data": [[49.98426249999997, 178.08955000000213]], "isOverall": false, "label": "Process transactional inline data requests-Aggregated", "isController": false}], "supportsControllersDiscrimination": true, "maxX": 50.0, "title": "Time VS Threads"}},
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
        data : {"result": {"minY": 0.0, "minX": 1.50969798E12, "maxY": 10228.75, "series": [{"data": [[1.50970668E12, 0.0], [1.50969798E12, 0.0], [1.5097002E12, 0.0], [1.50970242E12, 0.0], [1.50970464E12, 0.0], [1.50970566E12, 0.0], [1.509699E12, 0.0], [1.50970344E12, 0.0], [1.50970122E12, 0.0], [1.50970446E12, 0.0], [1.50970224E12, 0.0], [1.50970002E12, 0.0], [1.50970326E12, 0.0], [1.50970548E12, 0.0], [1.5097065E12, 0.0], [1.50970104E12, 0.0], [1.50969882E12, 0.0], [1.50970206E12, 0.0], [1.50970428E12, 0.0], [1.50969954E12, 0.0], [1.50970176E12, 0.0], [1.50970278E12, 0.0], [1.509705E12, 0.0], [1.50970158E12, 0.0], [1.50970602E12, 0.0], [1.5097038E12, 0.0], [1.50970722E12, 0.0], [1.50969834E12, 0.0], [1.50970056E12, 0.0], [1.50970038E12, 0.0], [1.50970482E12, 0.0], [1.50970686E12, 0.0], [1.50969936E12, 0.0], [1.5097026E12, 0.0], [1.50969918E12, 0.0], [1.5097014E12, 0.0], [1.50970362E12, 0.0], [1.50970584E12, 0.0], [1.50969816E12, 0.0], [1.50970704E12, 0.0], [1.5096999E12, 0.0], [1.50970434E12, 0.0], [1.50970638E12, 0.0], [1.50969888E12, 0.0], [1.50970212E12, 0.0], [1.50970314E12, 0.0], [1.50970536E12, 0.0], [1.50970758E12, 0.0], [1.5096987E12, 0.0], [1.50970092E12, 0.0], [1.50970656E12, 0.0], [1.50969972E12, 0.0], [1.5097062E12, 0.0], [1.50970416E12, 0.0], [1.50970194E12, 0.0], [1.50970518E12, 0.0], [1.5097074E12, 0.0], [1.50969852E12, 0.0], [1.50970296E12, 0.0], [1.50970074E12, 0.0], [1.50970398E12, 0.0], [1.50969924E12, 0.0], [1.50970368E12, 0.0], [1.50970146E12, 0.0], [1.5097047E12, 0.0], [1.50969804E12, 0.0], [1.50970248E12, 0.0], [1.50970692E12, 0.0], [1.50970026E12, 0.0], [1.5097035E12, 0.0], [1.50970572E12, 0.0], [1.50969906E12, 0.0], [1.50970128E12, 0.0], [1.5097023E12, 0.0], [1.50970452E12, 0.0], [1.5097011E12, 0.0], [1.50970554E12, 0.0], [1.50970332E12, 0.0], [1.50970008E12, 0.0], [1.50970674E12, 0.0], [1.50970182E12, 0.0], [1.50970404E12, 0.0], [1.50970728E12, 0.0], [1.50969858E12, 0.0], [1.5097008E12, 0.0], [1.50970062E12, 0.0], [1.50970506E12, 0.0], [1.5096996E12, 0.0], [1.50970284E12, 0.0], [1.50970626E12, 0.0], [1.50969942E12, 0.0], [1.50970164E12, 0.0], [1.50970386E12, 0.0], [1.50970608E12, 0.0], [1.5096984E12, 0.0], [1.50969822E12, 0.0], [1.50970044E12, 0.0], [1.50970266E12, 0.0], [1.50970488E12, 0.0], [1.5097071E12, 0.0], [1.5097059E12, 0.0], [1.50969894E12, 0.0], [1.50970116E12, 0.0], [1.50970338E12, 0.0], [1.5097056E12, 0.0], [1.50969996E12, 0.0], [1.50970218E12, 0.0], [1.5097044E12, 0.0], [1.50970662E12, 0.0], [1.50970542E12, 0.0], [1.5097032E12, 0.0], [1.50969876E12, 0.0], [1.50970098E12, 0.0], [1.50970422E12, 0.0], [1.50970746E12, 0.0], [1.509702E12, 0.0], [1.50970644E12, 0.0], [1.50969978E12, 0.0], [1.50970524E12, 0.0], [1.50970302E12, 0.0], [1.50969828E12, 0.0], [1.50970272E12, 0.0], [1.5097005E12, 0.0], [1.50970374E12, 0.0], [1.50970596E12, 0.0], [1.50970698E12, 0.0], [1.5096993E12, 0.0], [1.50970152E12, 0.0], [1.50970254E12, 0.0], [1.50970476E12, 0.0], [1.50970134E12, 0.0], [1.50970578E12, 0.0], [1.50970356E12, 0.0], [1.5097068E12, 0.0], [1.5096981E12, 0.0], [1.50970032E12, 0.0], [1.50970014E12, 0.0], [1.50970458E12, 0.0], [1.50969912E12, 0.0], [1.50970236E12, 0.0], [1.5097053E12, 0.0], [1.50970086E12, 0.0], [1.50970734E12, 0.0], [1.50969984E12, 0.0], [1.50970308E12, 0.0], [1.50970632E12, 0.0], [1.5097041E12, 0.0], [1.50969966E12, 0.0], [1.50970188E12, 0.0], [1.50970752E12, 0.0], [1.50969864E12, 0.0], [1.50970716E12, 0.0], [1.50969846E12, 0.0], [1.50970068E12, 0.0], [1.5097029E12, 0.0], [1.50970512E12, 0.0], [1.50970614E12, 0.0], [1.50969948E12, 0.0], [1.50970392E12, 0.0], [1.5097017E12, 0.0], [1.50970494E12, 0.0]], "isOverall": false, "label": "Bytes received per second", "isController": false}, {"data": [[1.50970668E12, 10208.333333333334], [1.50969798E12, 1364.4166666666667], [1.5097002E12, 10187.916666666666], [1.50970242E12, 10228.75], [1.50970464E12, 10208.333333333334], [1.50970566E12, 10228.75], [1.509699E12, 10220.4], [1.50970344E12, 10228.75], [1.50970122E12, 10228.75], [1.50970446E12, 10187.916666666666], [1.50970224E12, 10228.75], [1.50970002E12, 10228.75], [1.50970326E12, 10228.75], [1.50970548E12, 10228.75], [1.5097065E12, 10187.916666666666], [1.50970104E12, 10228.75], [1.50969882E12, 10220.4], [1.50970206E12, 10228.75], [1.50970428E12, 10208.333333333334], [1.50969954E12, 10208.333333333334], [1.50970176E12, 10208.333333333334], [1.50970278E12, 10208.333333333334], [1.509705E12, 10208.333333333334], [1.50970158E12, 10228.75], [1.50970602E12, 10228.75], [1.5097038E12, 10228.75], [1.50970722E12, 10208.333333333334], [1.50969834E12, 10200.0], [1.50970056E12, 10187.916666666666], [1.50970038E12, 10208.333333333334], [1.50970482E12, 10228.75], [1.50970686E12, 10187.916666666666], [1.50969936E12, 10228.75], [1.5097026E12, 10208.333333333334], [1.50969918E12, 10201.0], [1.5097014E12, 10208.333333333334], [1.50970362E12, 10187.916666666666], [1.50970584E12, 10208.333333333334], [1.50969816E12, 10200.0], [1.50970704E12, 10208.333333333334], [1.5096999E12, 10208.333333333334], [1.50970434E12, 10208.333333333334], [1.50970638E12, 10208.333333333334], [1.50969888E12, 10179.6], [1.50970212E12, 10187.916666666666], [1.50970314E12, 10208.333333333334], [1.50970536E12, 10187.916666666666], [1.50970758E12, 8983.333333333334], [1.5096987E12, 10179.6], [1.50970092E12, 10208.333333333334], [1.50970656E12, 10208.333333333334], [1.50969972E12, 10208.333333333334], [1.5097062E12, 10228.75], [1.50970416E12, 10208.333333333334], [1.50970194E12, 10228.75], [1.50970518E12, 10208.333333333334], [1.5097074E12, 10208.333333333334], [1.50969852E12, 10200.0], [1.50970296E12, 10228.75], [1.50970074E12, 10228.75], [1.50970398E12, 10208.333333333334], [1.50969924E12, 10228.75], [1.50970368E12, 10208.333333333334], [1.50970146E12, 10208.333333333334], [1.5097047E12, 10228.75], [1.50969804E12, 10028.066666666668], [1.50970248E12, 10208.333333333334], [1.50970692E12, 10228.75], [1.50970026E12, 10228.75], [1.5097035E12, 10208.333333333334], [1.50970572E12, 10187.916666666666], [1.50969906E12, 10179.6], [1.50970128E12, 10187.916666666666], [1.5097023E12, 10187.916666666666], [1.50970452E12, 10228.75], [1.5097011E12, 10187.916666666666], [1.50970554E12, 10208.333333333334], [1.50970332E12, 10187.916666666666], [1.50970008E12, 10208.333333333334], [1.50970674E12, 10208.333333333334], [1.50970182E12, 10228.75], [1.50970404E12, 10228.75], [1.50970728E12, 10187.916666666666], [1.50969858E12, 10200.0], [1.5097008E12, 10208.333333333334], [1.50970062E12, 10228.75], [1.50970506E12, 10187.916666666666], [1.5096996E12, 10228.75], [1.50970284E12, 10208.333333333334], [1.50970626E12, 10187.916666666666], [1.50969942E12, 10208.333333333334], [1.50970164E12, 10208.333333333334], [1.50970386E12, 10208.333333333334], [1.50970608E12, 10187.916666666666], [1.5096984E12, 10220.4], [1.50969822E12, 10179.6], [1.50970044E12, 10228.75], [1.50970266E12, 10208.333333333334], [1.50970488E12, 10208.333333333334], [1.5097071E12, 10187.916666666666], [1.5097059E12, 10208.333333333334], [1.50969894E12, 10200.0], [1.50970116E12, 10208.333333333334], [1.50970338E12, 10208.333333333334], [1.5097056E12, 10187.916666666666], [1.50969996E12, 10208.333333333334], [1.50970218E12, 10208.333333333334], [1.5097044E12, 10228.75], [1.50970662E12, 10228.75], [1.50970542E12, 10208.333333333334], [1.5097032E12, 10208.333333333334], [1.50969876E12, 10200.0], [1.50970098E12, 10208.333333333334], [1.50970422E12, 10208.333333333334], [1.50970746E12, 10187.916666666666], [1.509702E12, 10187.916666666666], [1.50970644E12, 10228.75], [1.50969978E12, 10208.333333333334], [1.50970524E12, 10187.916666666666], [1.50970302E12, 10208.333333333334], [1.50969828E12, 10200.0], [1.50970272E12, 10228.75], [1.5097005E12, 10208.333333333334], [1.50970374E12, 10208.333333333334], [1.50970596E12, 10187.916666666666], [1.50970698E12, 10208.333333333334], [1.5096993E12, 10187.916666666666], [1.50970152E12, 10208.333333333334], [1.50970254E12, 10187.916666666666], [1.50970476E12, 10187.916666666666], [1.50970134E12, 10208.333333333334], [1.50970578E12, 10228.75], [1.50970356E12, 10208.333333333334], [1.5097068E12, 10208.333333333334], [1.5096981E12, 10213.066666666668], [1.50970032E12, 10187.916666666666], [1.50970014E12, 10208.333333333334], [1.50970458E12, 10187.916666666666], [1.50969912E12, 10200.0], [1.50970236E12, 10208.333333333334], [1.5097053E12, 10228.75], [1.50970086E12, 10187.916666666666], [1.50970734E12, 10228.75], [1.50969984E12, 10208.333333333334], [1.50970308E12, 10187.916666666666], [1.50970632E12, 10208.333333333334], [1.5097041E12, 10187.916666666666], [1.50969966E12, 10187.916666666666], [1.50970188E12, 10187.916666666666], [1.50970752E12, 10228.75], [1.50969864E12, 10220.4], [1.50970716E12, 10228.75], [1.50969846E12, 10179.6], [1.50970068E12, 10187.916666666666], [1.5097029E12, 10187.916666666666], [1.50970512E12, 10228.75], [1.50970614E12, 10208.333333333334], [1.50969948E12, 10187.916666666666], [1.50970392E12, 10187.916666666666], [1.5097017E12, 10187.916666666666], [1.50970494E12, 10208.333333333334]], "isOverall": false, "label": "Bytes sent per second", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.50970758E12, "title": "Bytes Throughput Over Time"}},
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
        data: {"result": {"minY": 158.29399999999976, "minX": 1.50969798E12, "maxY": 794.8805970149255, "series": [{"data": [[1.50970668E12, 162.684], [1.50969798E12, 794.8805970149255], [1.5097002E12, 188.99599198396817], [1.50970242E12, 174.25748502994009], [1.50970464E12, 180.10000000000008], [1.50970566E12, 164.04191616766485], [1.509699E12, 188.16966067864288], [1.50970344E12, 179.9161676646708], [1.50970122E12, 179.89021956087825], [1.50970446E12, 178.2725450901803], [1.50970224E12, 176.25349301397196], [1.50970002E12, 179.4211576846306], [1.50970326E12, 188.441117764471], [1.50970548E12, 158.93413173652698], [1.5097065E12, 165.3186372745492], [1.50970104E12, 179.0219560878244], [1.50969882E12, 181.82834331337332], [1.50970206E12, 181.62874251496996], [1.50970428E12, 180.6640000000001], [1.50969954E12, 180.34999999999997], [1.50970176E12, 178.80999999999997], [1.50970278E12, 175.19800000000004], [1.509705E12, 187.0439999999999], [1.50970158E12, 180.9261477045907], [1.50970602E12, 163.42315369261473], [1.5097038E12, 184.99001996007985], [1.50970722E12, 162.88400000000001], [1.50969834E12, 179.99200000000002], [1.50970056E12, 207.79959919839675], [1.50970038E12, 179.60399999999976], [1.50970482E12, 182.54890219560892], [1.50970686E12, 172.76352705410824], [1.50969936E12, 182.8742514970059], [1.5097026E12, 186.23000000000002], [1.50969918E12, 180.9079999999999], [1.5097014E12, 190.09200000000018], [1.50970362E12, 180.24849699398806], [1.50970584E12, 160.79199999999983], [1.50969816E12, 188.96600000000012], [1.50970704E12, 163.58600000000004], [1.5096999E12, 202.54000000000005], [1.50970434E12, 180.974], [1.50970638E12, 182.40399999999997], [1.50969888E12, 181.00200400801597], [1.50970212E12, 179.0060120240481], [1.50970314E12, 177.63000000000017], [1.50970536E12, 161.8977955911826], [1.50970758E12, 162.6613636363636], [1.5096987E12, 184.12625250500997], [1.50970092E12, 175.07600000000002], [1.50970656E12, 161.62599999999998], [1.50969972E12, 185.9160000000001], [1.5097062E12, 189.53892215568854], [1.50970416E12, 178.3699999999998], [1.50970194E12, 179.89221556886235], [1.50970518E12, 158.89600000000007], [1.5097074E12, 171.81999999999994], [1.50969852E12, 178.272], [1.50970296E12, 180.1816367265467], [1.50970074E12, 177.52295409181636], [1.50970398E12, 176.47400000000005], [1.50969924E12, 180.2734530938122], [1.50970368E12, 177.61399999999992], [1.50970146E12, 183.03799999999998], [1.5097047E12, 182.01996007984022], [1.50969804E12, 182.92479674796758], [1.50970248E12, 172.25399999999996], [1.50970692E12, 165.01796407185623], [1.50970026E12, 181.63473053892218], [1.5097035E12, 183.31399999999996], [1.50970572E12, 163.35270541082167], [1.50969906E12, 185.75551102204417], [1.50970128E12, 178.07815631262534], [1.5097023E12, 185.28657314629265], [1.50970452E12, 177.9001996007986], [1.5097011E12, 184.90981963927848], [1.50970554E12, 159.2259999999999], [1.50970332E12, 182.33667334669354], [1.50970008E12, 176.374], [1.50970674E12, 158.29399999999976], [1.50970182E12, 180.8602794411178], [1.50970404E12, 175.73852295409188], [1.50970728E12, 160.86973947895783], [1.50969858E12, 189.41999999999993], [1.5097008E12, 188.50799999999987], [1.50970062E12, 177.311377245509], [1.50970506E12, 180.6252505010021], [1.5096996E12, 198.65868263473044], [1.50970284E12, 173.85599999999994], [1.50970626E12, 163.27855711422842], [1.50969942E12, 178.66800000000006], [1.50970164E12, 182.98200000000026], [1.50970386E12, 180.01800000000011], [1.50970608E12, 161.44288577154302], [1.5096984E12, 186.66467065868238], [1.50969822E12, 179.53907815631254], [1.50970044E12, 183.09381237524963], [1.50970266E12, 180.09000000000003], [1.50970488E12, 178.70199999999986], [1.5097071E12, 167.5390781563125], [1.5097059E12, 166.44200000000015], [1.50969894E12, 177.84199999999984], [1.50970116E12, 185.75000000000003], [1.50970338E12, 181.03399999999985], [1.5097056E12, 169.66933867735457], [1.50969996E12, 186.36200000000014], [1.50970218E12, 179.04600000000013], [1.5097044E12, 187.22554890219556], [1.50970662E12, 162.7804391217566], [1.50970542E12, 160.20799999999988], [1.5097032E12, 195.79999999999995], [1.50969876E12, 181.90000000000003], [1.50970098E12, 178.42799999999997], [1.50970422E12, 181.44799999999995], [1.50970746E12, 163.92585170340658], [1.509702E12, 186.98396793587185], [1.50970644E12, 160.8882235528943], [1.50969978E12, 189.46200000000002], [1.50970524E12, 167.58116232464934], [1.50970302E12, 178.09800000000016], [1.50969828E12, 182.4860000000001], [1.50970272E12, 176.36726546906198], [1.5097005E12, 195.26399999999998], [1.50970374E12, 190.67399999999995], [1.50970596E12, 162.78557114228465], [1.50970698E12, 164.48799999999997], [1.5096993E12, 184.87775551102206], [1.50970152E12, 179.962], [1.50970254E12, 180.48096192384756], [1.50970476E12, 182.0480961923847], [1.50970134E12, 176.20399999999987], [1.50970578E12, 162.98403193612768], [1.50970356E12, 179.48399999999995], [1.5097068E12, 176.8880000000002], [1.5096981E12, 183.46706586826355], [1.50970032E12, 179.27655310621233], [1.50970014E12, 179.85199999999986], [1.50970458E12, 176.7334669338676], [1.50969912E12, 180.31800000000004], [1.50970236E12, 170.53199999999995], [1.5097053E12, 164.76247504990022], [1.50970086E12, 182.78156312625254], [1.50970734E12, 161.01796407185637], [1.50969984E12, 187.66599999999985], [1.50970308E12, 176.99799599198394], [1.50970632E12, 161.54400000000024], [1.5097041E12, 182.6472945891784], [1.50969966E12, 189.685370741483], [1.50970188E12, 180.15230460921845], [1.50970752E12, 161.8143712574849], [1.50969864E12, 177.55089820359277], [1.50970716E12, 165.00000000000009], [1.50969846E12, 182.3507014028057], [1.50970068E12, 174.9278557114231], [1.5097029E12, 181.8777555110221], [1.50970512E12, 174.92614770459065], [1.50970614E12, 160.7520000000001], [1.50969948E12, 187.6092184368736], [1.50970392E12, 180.7194388777554], [1.5097017E12, 180.05410821643284], [1.50970494E12, 177.93800000000016]], "isOverall": false, "label": "Process transactional inline data requests", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.50970758E12, "title": "Response Time Over Time"}},
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
        data: {"result": {"minY": 157.966, "minX": 1.50969798E12, "maxY": 787.9253731343282, "series": [{"data": [[1.50970668E12, 162.246], [1.50969798E12, 787.9253731343282], [1.5097002E12, 188.58717434869743], [1.50970242E12, 173.90219560878228], [1.50970464E12, 179.732], [1.50970566E12, 163.70059880239518], [1.509699E12, 187.82235528942113], [1.50970344E12, 179.55688622754482], [1.50970122E12, 179.5568862275448], [1.50970446E12, 177.90180360721456], [1.50970224E12, 175.8882235528941], [1.50970002E12, 179.05389221556877], [1.50970326E12, 188.07984031936124], [1.50970548E12, 158.60279441117785], [1.5097065E12, 165.02004008016033], [1.50970104E12, 178.69061876247508], [1.50969882E12, 181.50099800399218], [1.50970206E12, 181.28542914171646], [1.50970428E12, 180.27200000000008], [1.50969954E12, 179.99], [1.50970176E12, 178.41], [1.50970278E12, 174.8460000000001], [1.509705E12, 186.642], [1.50970158E12, 180.47504990019937], [1.50970602E12, 163.06986027944112], [1.5097038E12, 184.67465069860268], [1.50970722E12, 162.55200000000005], [1.50969834E12, 179.50000000000003], [1.50970056E12, 207.4128256513026], [1.50970038E12, 179.23399999999998], [1.50970482E12, 182.17365269461095], [1.50970686E12, 172.430861723447], [1.50969936E12, 182.45908183632736], [1.5097026E12, 185.8400000000001], [1.50969918E12, 180.55800000000002], [1.5097014E12, 189.73799999999986], [1.50970362E12, 179.85170340681378], [1.50970584E12, 160.50600000000006], [1.50969816E12, 188.60400000000004], [1.50970704E12, 163.22400000000007], [1.5096999E12, 202.17200000000003], [1.50970434E12, 180.58399999999972], [1.50970638E12, 182.01599999999996], [1.50969888E12, 180.69338677354708], [1.50970212E12, 178.6673346693388], [1.50970314E12, 177.158], [1.50970536E12, 161.55911823647284], [1.50970758E12, 162.3522727272728], [1.5096987E12, 183.80961923847693], [1.50970092E12, 174.74799999999985], [1.50970656E12, 161.27999999999997], [1.50969972E12, 185.504], [1.5097062E12, 189.18962075848304], [1.50970416E12, 178.006], [1.50970194E12, 179.5029940119762], [1.50970518E12, 158.5780000000001], [1.5097074E12, 171.49399999999977], [1.50969852E12, 177.88599999999994], [1.50970296E12, 179.79840319361278], [1.50970074E12, 177.1716566866266], [1.50970398E12, 176.13200000000006], [1.50969924E12, 179.94610778443126], [1.50970368E12, 177.27800000000016], [1.50970146E12, 182.676], [1.5097047E12, 181.63872255489042], [1.50969804E12, 182.5813008130081], [1.50970248E12, 171.94599999999997], [1.50970692E12, 164.70658682634752], [1.50970026E12, 181.29141716566863], [1.5097035E12, 182.9519999999999], [1.50970572E12, 163.07414829659328], [1.50969906E12, 185.38476953907838], [1.50970128E12, 177.71342685370735], [1.5097023E12, 184.86172344689382], [1.50970452E12, 177.51097804391208], [1.5097011E12, 184.57515030060105], [1.50970554E12, 158.91199999999995], [1.50970332E12, 181.9619238476953], [1.50970008E12, 175.99800000000002], [1.50970674E12, 157.966], [1.50970182E12, 180.49301397205585], [1.50970404E12, 175.4131736526947], [1.50970728E12, 160.56513026052093], [1.50969858E12, 189.01600000000008], [1.5097008E12, 188.1019999999999], [1.50970062E12, 176.9720558882236], [1.50970506E12, 180.2925851703407], [1.5096996E12, 198.28542914171675], [1.50970284E12, 173.51200000000009], [1.50970626E12, 162.96593186372752], [1.50969942E12, 178.294], [1.50970164E12, 182.55600000000004], [1.50970386E12, 179.6480000000001], [1.50970608E12, 161.10621242484987], [1.5096984E12, 186.31736526946113], [1.50969822E12, 179.196392785571], [1.50970044E12, 182.70259481037903], [1.50970266E12, 179.75200000000018], [1.50970488E12, 178.272], [1.5097071E12, 167.1923847695392], [1.5097059E12, 166.11400000000015], [1.50969894E12, 177.4939999999999], [1.50970116E12, 185.3879999999999], [1.50970338E12, 180.65399999999994], [1.5097056E12, 169.35671342685367], [1.50969996E12, 185.95800000000008], [1.50970218E12, 178.67199999999977], [1.5097044E12, 186.83233532934125], [1.50970662E12, 162.4710578842314], [1.50970542E12, 159.88599999999977], [1.5097032E12, 195.3680000000001], [1.50969876E12, 181.53199999999995], [1.50970098E12, 178.01799999999983], [1.50970422E12, 181.07399999999998], [1.50970746E12, 163.61723446893785], [1.509702E12, 186.63326653306606], [1.50970644E12, 160.5229540918163], [1.50969978E12, 189.0439999999999], [1.50970524E12, 167.25050100200414], [1.50970302E12, 177.71399999999997], [1.50969828E12, 182.10399999999998], [1.50970272E12, 176.0179640718564], [1.5097005E12, 194.85600000000008], [1.50970374E12, 190.33000000000013], [1.50970596E12, 162.4949899799599], [1.50970698E12, 164.13], [1.5096993E12, 184.53907815631266], [1.50970152E12, 179.578], [1.50970254E12, 180.1503006012024], [1.50970476E12, 181.62324649298597], [1.50970134E12, 175.81000000000012], [1.50970578E12, 162.65868263473058], [1.50970356E12, 179.10399999999996], [1.5097068E12, 176.52399999999986], [1.5096981E12, 183.07784431137713], [1.50970032E12, 178.8737474949899], [1.50970014E12, 179.35400000000004], [1.50970458E12, 176.3807615230461], [1.50969912E12, 179.99200000000002], [1.50970236E12, 170.16599999999997], [1.5097053E12, 164.43512974051885], [1.50970086E12, 182.4248496993988], [1.50970734E12, 160.6726546906188], [1.50969984E12, 187.26599999999985], [1.50970308E12, 176.62324649298594], [1.50970632E12, 161.182], [1.5097041E12, 182.2284569138278], [1.50969966E12, 189.25651302605206], [1.50970188E12, 179.7374749498997], [1.50970752E12, 161.4750499001995], [1.50969864E12, 177.22355289421174], [1.50970716E12, 164.6786427145708], [1.50969846E12, 181.97995991983947], [1.50970068E12, 174.61122244488988], [1.5097029E12, 181.48096192384753], [1.50970512E12, 174.5868263473052], [1.50970614E12, 160.40800000000004], [1.50969948E12, 187.19238476953907], [1.50970392E12, 180.37274549098203], [1.5097017E12, 179.7354709418837], [1.50970494E12, 177.56399999999985]], "isOverall": false, "label": "Process transactional inline data requests", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.50970758E12, "title": "Latencies Over Time"}},
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
    data: {"result": {"minY": 161.0, "minX": 67.0, "maxY": 921.0, "series": [{"data": [[67.0, 921.0], [440.0, 161.0], [492.0, 177.0], [501.0, 169.0], [500.0, 171.0], [499.0, 170.0]], "isOverall": false, "label": "Successes", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 501.0, "title": "Response Time Vs Request"}},
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
    data: {"result": {"minY": 161.0, "minX": 67.0, "maxY": 914.0, "series": [{"data": [[67.0, 914.0], [440.0, 161.0], [492.0, 177.0], [501.0, 169.0], [500.0, 170.0], [499.0, 170.0]], "isOverall": false, "label": "Successes", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 501.0, "title": "Latencies Vs Request"}},
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
        data: {"result": {"minY": 1.15, "minX": 1.50969798E12, "maxY": 8.35, "series": [{"data": [[1.50970668E12, 8.333333333333334], [1.50969798E12, 1.15], [1.5097002E12, 8.333333333333334], [1.50970242E12, 8.333333333333334], [1.50970464E12, 8.333333333333334], [1.50970566E12, 8.333333333333334], [1.509699E12, 8.333333333333334], [1.50970344E12, 8.333333333333334], [1.50970122E12, 8.333333333333334], [1.50970446E12, 8.333333333333334], [1.50970224E12, 8.333333333333334], [1.50970002E12, 8.333333333333334], [1.50970326E12, 8.333333333333334], [1.50970548E12, 8.333333333333334], [1.5097065E12, 8.333333333333334], [1.50970104E12, 8.333333333333334], [1.50969882E12, 8.333333333333334], [1.50970206E12, 8.333333333333334], [1.50970428E12, 8.333333333333334], [1.50969954E12, 8.333333333333334], [1.50970176E12, 8.333333333333334], [1.50970278E12, 8.333333333333334], [1.509705E12, 8.333333333333334], [1.50970158E12, 8.333333333333334], [1.50970602E12, 8.333333333333334], [1.5097038E12, 8.333333333333334], [1.50970722E12, 8.333333333333334], [1.50969834E12, 8.333333333333334], [1.50970056E12, 8.333333333333334], [1.50970038E12, 8.333333333333334], [1.50970482E12, 8.333333333333334], [1.50970686E12, 8.333333333333334], [1.50969936E12, 8.333333333333334], [1.5097026E12, 8.333333333333334], [1.50969918E12, 8.333333333333334], [1.5097014E12, 8.333333333333334], [1.50970362E12, 8.333333333333334], [1.50970584E12, 8.333333333333334], [1.50969816E12, 8.333333333333334], [1.50970704E12, 8.333333333333334], [1.5096999E12, 8.333333333333334], [1.50970434E12, 8.333333333333334], [1.50970638E12, 8.333333333333334], [1.50969888E12, 8.333333333333334], [1.50970212E12, 8.333333333333334], [1.50970314E12, 8.333333333333334], [1.50970536E12, 8.333333333333334], [1.50970758E12, 7.316666666666666], [1.5096987E12, 8.333333333333334], [1.50970092E12, 8.333333333333334], [1.50970656E12, 8.333333333333334], [1.50969972E12, 8.333333333333334], [1.5097062E12, 8.333333333333334], [1.50970416E12, 8.333333333333334], [1.50970194E12, 8.333333333333334], [1.50970518E12, 8.333333333333334], [1.5097074E12, 8.333333333333334], [1.50969852E12, 8.333333333333334], [1.50970296E12, 8.333333333333334], [1.50970074E12, 8.333333333333334], [1.50970398E12, 8.333333333333334], [1.50969924E12, 8.333333333333334], [1.50970368E12, 8.333333333333334], [1.50970146E12, 8.333333333333334], [1.5097047E12, 8.333333333333334], [1.50969804E12, 8.2], [1.50970248E12, 8.333333333333334], [1.50970692E12, 8.333333333333334], [1.50970026E12, 8.333333333333334], [1.5097035E12, 8.333333333333334], [1.50970572E12, 8.333333333333334], [1.50969906E12, 8.333333333333334], [1.50970128E12, 8.333333333333334], [1.5097023E12, 8.35], [1.50970452E12, 8.333333333333334], [1.5097011E12, 8.333333333333334], [1.50970554E12, 8.333333333333334], [1.50970332E12, 8.333333333333334], [1.50970008E12, 8.333333333333334], [1.50970674E12, 8.333333333333334], [1.50970182E12, 8.333333333333334], [1.50970404E12, 8.333333333333334], [1.50970728E12, 8.333333333333334], [1.50969858E12, 8.333333333333334], [1.5097008E12, 8.333333333333334], [1.50970062E12, 8.333333333333334], [1.50970506E12, 8.333333333333334], [1.5096996E12, 8.333333333333334], [1.50970284E12, 8.333333333333334], [1.50970626E12, 8.333333333333334], [1.50969942E12, 8.333333333333334], [1.50970164E12, 8.333333333333334], [1.50970386E12, 8.333333333333334], [1.50970608E12, 8.333333333333334], [1.5096984E12, 8.333333333333334], [1.50969822E12, 8.333333333333334], [1.50970044E12, 8.333333333333334], [1.50970266E12, 8.333333333333334], [1.50970488E12, 8.333333333333334], [1.5097071E12, 8.333333333333334], [1.5097059E12, 8.333333333333334], [1.50969894E12, 8.333333333333334], [1.50970116E12, 8.333333333333334], [1.50970338E12, 8.333333333333334], [1.5097056E12, 8.333333333333334], [1.50969996E12, 8.333333333333334], [1.50970218E12, 8.333333333333334], [1.5097044E12, 8.333333333333334], [1.50970662E12, 8.333333333333334], [1.50970542E12, 8.333333333333334], [1.5097032E12, 8.333333333333334], [1.50969876E12, 8.333333333333334], [1.50970098E12, 8.333333333333334], [1.50970422E12, 8.333333333333334], [1.50970746E12, 8.333333333333334], [1.509702E12, 8.333333333333334], [1.50970644E12, 8.333333333333334], [1.50969978E12, 8.333333333333334], [1.50970524E12, 8.333333333333334], [1.50970302E12, 8.333333333333334], [1.50969828E12, 8.333333333333334], [1.50970272E12, 8.333333333333334], [1.5097005E12, 8.333333333333334], [1.50970374E12, 8.333333333333334], [1.50970596E12, 8.333333333333334], [1.50970698E12, 8.333333333333334], [1.5096993E12, 8.333333333333334], [1.50970152E12, 8.333333333333334], [1.50970254E12, 8.333333333333334], [1.50970476E12, 8.333333333333334], [1.50970134E12, 8.333333333333334], [1.50970578E12, 8.333333333333334], [1.50970356E12, 8.333333333333334], [1.5097068E12, 8.333333333333334], [1.5096981E12, 8.333333333333334], [1.50970032E12, 8.333333333333334], [1.50970014E12, 8.333333333333334], [1.50970458E12, 8.333333333333334], [1.50969912E12, 8.333333333333334], [1.50970236E12, 8.316666666666666], [1.5097053E12, 8.333333333333334], [1.50970086E12, 8.333333333333334], [1.50970734E12, 8.333333333333334], [1.50969984E12, 8.333333333333334], [1.50970308E12, 8.333333333333334], [1.50970632E12, 8.333333333333334], [1.5097041E12, 8.333333333333334], [1.50969966E12, 8.333333333333334], [1.50970188E12, 8.333333333333334], [1.50970752E12, 8.333333333333334], [1.50969864E12, 8.333333333333334], [1.50970716E12, 8.333333333333334], [1.50969846E12, 8.333333333333334], [1.50970068E12, 8.333333333333334], [1.5097029E12, 8.333333333333334], [1.50970512E12, 8.333333333333334], [1.50970614E12, 8.333333333333334], [1.50969948E12, 8.333333333333334], [1.50970392E12, 8.333333333333334], [1.5097017E12, 8.333333333333334], [1.50970494E12, 8.333333333333334]], "isOverall": false, "label": "hitsPerSecond", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.50970758E12, "title": "Hits Per Second"}},
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
        data: {"result": {"minY": 1.1166666666666667, "minX": 1.50969798E12, "maxY": 8.35, "series": [{"data": [[1.50970668E12, 8.333333333333334], [1.50969798E12, 1.1166666666666667], [1.5097002E12, 8.316666666666666], [1.50970242E12, 8.35], [1.50970464E12, 8.333333333333334], [1.50970566E12, 8.35], [1.509699E12, 8.35], [1.50970344E12, 8.35], [1.50970122E12, 8.35], [1.50970446E12, 8.316666666666666], [1.50970224E12, 8.35], [1.50970002E12, 8.35], [1.50970326E12, 8.35], [1.50970548E12, 8.35], [1.5097065E12, 8.316666666666666], [1.50970104E12, 8.35], [1.50969882E12, 8.35], [1.50970206E12, 8.35], [1.50970428E12, 8.333333333333334], [1.50969954E12, 8.333333333333334], [1.50970176E12, 8.333333333333334], [1.50970278E12, 8.333333333333334], [1.509705E12, 8.333333333333334], [1.50970158E12, 8.35], [1.50970602E12, 8.35], [1.5097038E12, 8.35], [1.50970722E12, 8.333333333333334], [1.50969834E12, 8.333333333333334], [1.50970056E12, 8.316666666666666], [1.50970038E12, 8.333333333333334], [1.50970482E12, 8.35], [1.50970686E12, 8.316666666666666], [1.50969936E12, 8.35], [1.5097026E12, 8.333333333333334], [1.50969918E12, 8.333333333333334], [1.5097014E12, 8.333333333333334], [1.50970362E12, 8.316666666666666], [1.50970584E12, 8.333333333333334], [1.50969816E12, 8.333333333333334], [1.50970704E12, 8.333333333333334], [1.5096999E12, 8.333333333333334], [1.50970434E12, 8.333333333333334], [1.50970638E12, 8.333333333333334], [1.50969888E12, 8.316666666666666], [1.50970212E12, 8.316666666666666], [1.50970314E12, 8.333333333333334], [1.50970536E12, 8.316666666666666], [1.50970758E12, 7.333333333333333], [1.5096987E12, 8.316666666666666], [1.50970092E12, 8.333333333333334], [1.50970656E12, 8.333333333333334], [1.50969972E12, 8.333333333333334], [1.5097062E12, 8.35], [1.50970416E12, 8.333333333333334], [1.50970194E12, 8.35], [1.50970518E12, 8.333333333333334], [1.5097074E12, 8.333333333333334], [1.50969852E12, 8.333333333333334], [1.50970296E12, 8.35], [1.50970074E12, 8.35], [1.50970398E12, 8.333333333333334], [1.50969924E12, 8.35], [1.50970368E12, 8.333333333333334], [1.50970146E12, 8.333333333333334], [1.5097047E12, 8.35], [1.50969804E12, 8.2], [1.50970248E12, 8.333333333333334], [1.50970692E12, 8.35], [1.50970026E12, 8.35], [1.5097035E12, 8.333333333333334], [1.50970572E12, 8.316666666666666], [1.50969906E12, 8.316666666666666], [1.50970128E12, 8.316666666666666], [1.5097023E12, 8.316666666666666], [1.50970452E12, 8.35], [1.5097011E12, 8.316666666666666], [1.50970554E12, 8.333333333333334], [1.50970332E12, 8.316666666666666], [1.50970008E12, 8.333333333333334], [1.50970674E12, 8.333333333333334], [1.50970182E12, 8.35], [1.50970404E12, 8.35], [1.50970728E12, 8.316666666666666], [1.50969858E12, 8.333333333333334], [1.5097008E12, 8.333333333333334], [1.50970062E12, 8.35], [1.50970506E12, 8.316666666666666], [1.5096996E12, 8.35], [1.50970284E12, 8.333333333333334], [1.50970626E12, 8.316666666666666], [1.50969942E12, 8.333333333333334], [1.50970164E12, 8.333333333333334], [1.50970386E12, 8.333333333333334], [1.50970608E12, 8.316666666666666], [1.5096984E12, 8.35], [1.50969822E12, 8.316666666666666], [1.50970044E12, 8.35], [1.50970266E12, 8.333333333333334], [1.50970488E12, 8.333333333333334], [1.5097071E12, 8.316666666666666], [1.5097059E12, 8.333333333333334], [1.50969894E12, 8.333333333333334], [1.50970116E12, 8.333333333333334], [1.50970338E12, 8.333333333333334], [1.5097056E12, 8.316666666666666], [1.50969996E12, 8.333333333333334], [1.50970218E12, 8.333333333333334], [1.5097044E12, 8.35], [1.50970662E12, 8.35], [1.50970542E12, 8.333333333333334], [1.5097032E12, 8.333333333333334], [1.50969876E12, 8.333333333333334], [1.50970098E12, 8.333333333333334], [1.50970422E12, 8.333333333333334], [1.50970746E12, 8.316666666666666], [1.509702E12, 8.316666666666666], [1.50970644E12, 8.35], [1.50969978E12, 8.333333333333334], [1.50970524E12, 8.316666666666666], [1.50970302E12, 8.333333333333334], [1.50969828E12, 8.333333333333334], [1.50970272E12, 8.35], [1.5097005E12, 8.333333333333334], [1.50970374E12, 8.333333333333334], [1.50970596E12, 8.316666666666666], [1.50970698E12, 8.333333333333334], [1.5096993E12, 8.316666666666666], [1.50970152E12, 8.333333333333334], [1.50970254E12, 8.316666666666666], [1.50970476E12, 8.316666666666666], [1.50970134E12, 8.333333333333334], [1.50970578E12, 8.35], [1.50970356E12, 8.333333333333334], [1.5097068E12, 8.333333333333334], [1.5096981E12, 8.35], [1.50970032E12, 8.316666666666666], [1.50970014E12, 8.333333333333334], [1.50970458E12, 8.316666666666666], [1.50969912E12, 8.333333333333334], [1.50970236E12, 8.333333333333334], [1.5097053E12, 8.35], [1.50970086E12, 8.316666666666666], [1.50970734E12, 8.35], [1.50969984E12, 8.333333333333334], [1.50970308E12, 8.316666666666666], [1.50970632E12, 8.333333333333334], [1.5097041E12, 8.316666666666666], [1.50969966E12, 8.316666666666666], [1.50970188E12, 8.316666666666666], [1.50970752E12, 8.35], [1.50969864E12, 8.35], [1.50970716E12, 8.35], [1.50969846E12, 8.316666666666666], [1.50970068E12, 8.316666666666666], [1.5097029E12, 8.316666666666666], [1.50970512E12, 8.35], [1.50970614E12, 8.333333333333334], [1.50969948E12, 8.316666666666666], [1.50970392E12, 8.316666666666666], [1.5097017E12, 8.316666666666666], [1.50970494E12, 8.333333333333334]], "isOverall": false, "label": "200", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.50970758E12, "title": "Codes Per Second"}},
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
        data: {"result": {"minY": 1.1166666666666667, "minX": 1.50969798E12, "maxY": 8.35, "series": [{"data": [[1.50970668E12, 8.333333333333334], [1.50969798E12, 1.1166666666666667], [1.5097002E12, 8.316666666666666], [1.50970242E12, 8.35], [1.50970464E12, 8.333333333333334], [1.50970566E12, 8.35], [1.509699E12, 8.35], [1.50970344E12, 8.35], [1.50970122E12, 8.35], [1.50970446E12, 8.316666666666666], [1.50970224E12, 8.35], [1.50970002E12, 8.35], [1.50970326E12, 8.35], [1.50970548E12, 8.35], [1.5097065E12, 8.316666666666666], [1.50970104E12, 8.35], [1.50969882E12, 8.35], [1.50970206E12, 8.35], [1.50970428E12, 8.333333333333334], [1.50969954E12, 8.333333333333334], [1.50970176E12, 8.333333333333334], [1.50970278E12, 8.333333333333334], [1.509705E12, 8.333333333333334], [1.50970158E12, 8.35], [1.50970602E12, 8.35], [1.5097038E12, 8.35], [1.50970722E12, 8.333333333333334], [1.50969834E12, 8.333333333333334], [1.50970056E12, 8.316666666666666], [1.50970038E12, 8.333333333333334], [1.50970482E12, 8.35], [1.50970686E12, 8.316666666666666], [1.50969936E12, 8.35], [1.5097026E12, 8.333333333333334], [1.50969918E12, 8.333333333333334], [1.5097014E12, 8.333333333333334], [1.50970362E12, 8.316666666666666], [1.50970584E12, 8.333333333333334], [1.50969816E12, 8.333333333333334], [1.50970704E12, 8.333333333333334], [1.5096999E12, 8.333333333333334], [1.50970434E12, 8.333333333333334], [1.50970638E12, 8.333333333333334], [1.50969888E12, 8.316666666666666], [1.50970212E12, 8.316666666666666], [1.50970314E12, 8.333333333333334], [1.50970536E12, 8.316666666666666], [1.50970758E12, 7.333333333333333], [1.5096987E12, 8.316666666666666], [1.50970092E12, 8.333333333333334], [1.50970656E12, 8.333333333333334], [1.50969972E12, 8.333333333333334], [1.5097062E12, 8.35], [1.50970416E12, 8.333333333333334], [1.50970194E12, 8.35], [1.50970518E12, 8.333333333333334], [1.5097074E12, 8.333333333333334], [1.50969852E12, 8.333333333333334], [1.50970296E12, 8.35], [1.50970074E12, 8.35], [1.50970398E12, 8.333333333333334], [1.50969924E12, 8.35], [1.50970368E12, 8.333333333333334], [1.50970146E12, 8.333333333333334], [1.5097047E12, 8.35], [1.50969804E12, 8.2], [1.50970248E12, 8.333333333333334], [1.50970692E12, 8.35], [1.50970026E12, 8.35], [1.5097035E12, 8.333333333333334], [1.50970572E12, 8.316666666666666], [1.50969906E12, 8.316666666666666], [1.50970128E12, 8.316666666666666], [1.5097023E12, 8.316666666666666], [1.50970452E12, 8.35], [1.5097011E12, 8.316666666666666], [1.50970554E12, 8.333333333333334], [1.50970332E12, 8.316666666666666], [1.50970008E12, 8.333333333333334], [1.50970674E12, 8.333333333333334], [1.50970182E12, 8.35], [1.50970404E12, 8.35], [1.50970728E12, 8.316666666666666], [1.50969858E12, 8.333333333333334], [1.5097008E12, 8.333333333333334], [1.50970062E12, 8.35], [1.50970506E12, 8.316666666666666], [1.5096996E12, 8.35], [1.50970284E12, 8.333333333333334], [1.50970626E12, 8.316666666666666], [1.50969942E12, 8.333333333333334], [1.50970164E12, 8.333333333333334], [1.50970386E12, 8.333333333333334], [1.50970608E12, 8.316666666666666], [1.5096984E12, 8.35], [1.50969822E12, 8.316666666666666], [1.50970044E12, 8.35], [1.50970266E12, 8.333333333333334], [1.50970488E12, 8.333333333333334], [1.5097071E12, 8.316666666666666], [1.5097059E12, 8.333333333333334], [1.50969894E12, 8.333333333333334], [1.50970116E12, 8.333333333333334], [1.50970338E12, 8.333333333333334], [1.5097056E12, 8.316666666666666], [1.50969996E12, 8.333333333333334], [1.50970218E12, 8.333333333333334], [1.5097044E12, 8.35], [1.50970662E12, 8.35], [1.50970542E12, 8.333333333333334], [1.5097032E12, 8.333333333333334], [1.50969876E12, 8.333333333333334], [1.50970098E12, 8.333333333333334], [1.50970422E12, 8.333333333333334], [1.50970746E12, 8.316666666666666], [1.509702E12, 8.316666666666666], [1.50970644E12, 8.35], [1.50969978E12, 8.333333333333334], [1.50970524E12, 8.316666666666666], [1.50970302E12, 8.333333333333334], [1.50969828E12, 8.333333333333334], [1.50970272E12, 8.35], [1.5097005E12, 8.333333333333334], [1.50970374E12, 8.333333333333334], [1.50970596E12, 8.316666666666666], [1.50970698E12, 8.333333333333334], [1.5096993E12, 8.316666666666666], [1.50970152E12, 8.333333333333334], [1.50970254E12, 8.316666666666666], [1.50970476E12, 8.316666666666666], [1.50970134E12, 8.333333333333334], [1.50970578E12, 8.35], [1.50970356E12, 8.333333333333334], [1.5097068E12, 8.333333333333334], [1.5096981E12, 8.35], [1.50970032E12, 8.316666666666666], [1.50970014E12, 8.333333333333334], [1.50970458E12, 8.316666666666666], [1.50969912E12, 8.333333333333334], [1.50970236E12, 8.333333333333334], [1.5097053E12, 8.35], [1.50970086E12, 8.316666666666666], [1.50970734E12, 8.35], [1.50969984E12, 8.333333333333334], [1.50970308E12, 8.316666666666666], [1.50970632E12, 8.333333333333334], [1.5097041E12, 8.316666666666666], [1.50969966E12, 8.316666666666666], [1.50970188E12, 8.316666666666666], [1.50970752E12, 8.35], [1.50969864E12, 8.35], [1.50970716E12, 8.35], [1.50969846E12, 8.316666666666666], [1.50970068E12, 8.316666666666666], [1.5097029E12, 8.316666666666666], [1.50970512E12, 8.35], [1.50970614E12, 8.333333333333334], [1.50969948E12, 8.316666666666666], [1.50970392E12, 8.316666666666666], [1.5097017E12, 8.316666666666666], [1.50970494E12, 8.333333333333334]], "isOverall": false, "label": "Process transactional inline data requests-success", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.50970758E12, "title": "Transactions Per Second"}},
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
