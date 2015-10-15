<!doctype html>
<html ng-app="app">
<head>
    <script src="angular.js"></script>
    <script src="angular-touch.js"></script>
    <script src="angular-animate.js"></script>
    <script src="csv.js"></script>
    <script src="pdfmake.js"></script>
    <script src="vfs_fonts.js"></script>
    <script src="ui-grid.js"></script>
    <script src="app.js"></script>

    <script src="<%=request.getContextPath()%>/lightlink-api/jsapi.js"></script>

    <link rel="stylesheet" type="text/css" href="ui-grid.css"/>

    <style>
        .grid, .results{
            margin: 20px;
            width: 850px;
        }
        div.grid{
            height: 600px;
        }
        body,th,td,div{
            font-family: sans-serif;
            font-size: small;
        }
        th{
            background-color: #333377;
            color: white;
            padding: 5px;
        }

    </style>
</head>
<body>
<div ng-controller="MainCtrl">

    <table class="results" border="1" cellspacing=0 style="border-color: #FFFFFF">
        <tr>
            <th> Architecture / Stack </th>
            <th> Action</th>
            <th>First page display time</th>
            <th>Whole result set loading time</th>
        </tr> <tr>
            <td> Spring JDBCTemplate, Standard REST</td>
            <td> <button id="loadSpringRest" ng-click="loadSpringRest()"
                         class="btn btn-success">Test
            </button></td>
            <td>{{time.loadSpringRest}}</td>
            <td>{{time.loadSpringRestAll}}</td>
        </tr> <tr>
            <td> Spring JDBCTemplate + LightLink Streaming</td>
            <td> <button id="loadStandard" ng-click="loadStandard()"
                         class="btn btn-success">Test
            </button></td>
            <td>{{time.loadStandard}}</td>
            <td>{{time.loadStandardAll}}</td>
        </tr> <tr>
            <td> Spring JDBCTemplate + LightLink Streaming + Progressive Loading</td>
            <td> <button id="loadProgressive" ng-click="loadProgressive()"
                         class="btn btn-success">Test
            </button></td>
            <td>{{time.loadProgressive}}</td>
            <td>{{time.loadProgressiveAll}}</td>
        </tr>
    </table>

        <div id="grid1" ui-grid="gridOptions" class="grid"></div>

</div>
</body>
</html>