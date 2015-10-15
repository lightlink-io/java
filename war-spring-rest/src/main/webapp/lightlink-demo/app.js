var app = angular.module('app', ['ngAnimate', 'ngTouch', 'ui.grid']);

app.controller('MainCtrl', ['$scope', '$http', 'uiGridConstants', function ($scope, $http, uiGridConstants) {
    $scope.gridOptions = {
        enableFiltering: true,
        flatEntityAccess: true,
        showGridFooter: true,
        fastWatch: true
    };

    $scope.gridOptions.columnDefs = [
        {name: 'id'},
        {name: 'name'},
        {name: 'gender'},
        {field: 'age'}
    ];

    $scope.time = {
        loadProgressive: 0,
        loadStandard: 0,
        loadSpringRest: 0,
        loadProgressiveAll: 0,
        loadStandardAll: 0,
        loadSpringRestAll: 0
    };


    $scope.loadProgressiveJSSQL = function () {
        var t = new Date().getTime();
        $scope.time.loadProgressive = "";
        $scope.time.loadProgressiveAll = "";
        $scope.gridOptions.data = [];
        demo.angular.loadNames({}, function (res, isPartial) {
            $scope.gridOptions.data = res.resultSet;
            if (!$scope.time.loadProgressive)
                $scope.time.loadProgressive = $scope.time.loadProgressiveAll = ((new Date().getTime() - t) / 1000) + "sec";
            $scope.$apply();
        }, this, {progressive: [30, 100]})


    };
    $scope.loadProgressive = function () {
        var t = new Date().getTime();
        $scope.time.loadProgressive = "";
        $scope.time.loadProgressiveAll = "";
        $scope.gridOptions.data = [];
        employeeController.employeesStreamWithoutMapping({}, function (res, isPartial) {
            $scope.gridOptions.data = res.resultSet;
            if (!$scope.time.loadProgressive)
                $scope.time.loadProgressive = ((new Date().getTime() - t) / 1000) + "sec";
            if (!isPartial && !$scope.time.loadProgressiveAll)
                $scope.time.loadProgressiveAll = ((new Date().getTime() - t) / 1000) + "sec";
            $scope.$apply();
        }, this, {progressive: [30, 100]})


    };
    $scope.loadStandard = function () {
        var t = new Date().getTime();
        $scope.time.loadStandard="";
        $scope.time.loadStandardAll="";
        $scope.gridOptions.data=[];

        employeeController.employeesStreamWithoutMapping({}, function (res) {
            $scope.gridOptions.data = res.resultSet;
            if (!$scope.time.loadStandard)
                $scope.time.loadStandard =$scope.time.loadStandardAll = ((new Date().getTime() - t) / 1000) + "sec";
            $scope.$apply();
        }, this);
    };

    $scope.loadSpringRest = function () {

        var t = new Date().getTime();
        $scope.time.loadSpringRest = "";
        $scope.time.loadSpringRestAll = "";
        $scope.gridOptions.data = [];

        $http.get('../action/data/loadNames')
            .success(function (data) {
                $scope.gridOptions.data = data;
                $scope.time.loadSpringRest =$scope.time.loadSpringRestAll = (new Date().getTime() - t) / 1000 + " sec";
            });

    }
}]);