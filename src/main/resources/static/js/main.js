var myApp=angular.module('houseApp', ['ngRoute','ui.bootstrap']);
myApp.filter("housePrice", function() {
    return function(input) {
        return input / 10000 + "万";
    };
}).filter("unitPrice", function() {
    return function(input) {
        if(input == undefined)
            return "";
        return input.toFixed(0) + "元";
    };
});
myApp.controller('rootCtrl', function() {

}).config(function($routeProvider) {
    $routeProvider.
    when('/prices', {
        controller: 'pricesChange',
        templateUrl: '/prices.html',
        title: '价格变动',
        pageSlug: 'prices'
    }).
    when('/house/diff', {
        controller: 'houseDiff',
        templateUrl: '/houseDiff.html',
        title: '成交停售查询',
        pageSlug: 'houseDiff'
    }).otherwise({
        redirectTo: '/'
    });
});

myApp.controller('pricesChange', function($scope, $http, $filter) {
    $scope.menuType = 'prices';
    var curDate = new Date();
    curDate.setDate(curDate.getDate() - 1);
    $scope.endDate = curDate;
    var startDate = new Date();
    startDate.setDate(curDate.getDate() - 1);
    $scope.startDate = startDate;
    $scope.ajax = false;
    $scope.up = false;
    $http.get("/prices/changes")
        .success(function(res) {
            $scope.size = res.size;
            $scope.houseItems = res.houseItems;
            $scope.circle = res.circle;
            $scope.ajax = true;
        });
    $scope.pricesChange = function() {
        $scope.ajax = false;
        var startDateStr = $filter('date')($scope.startDate, 'yyyy-MM-dd');
        var endDateStr = $filter('date')($scope.endDate, 'yyyy-MM-dd');
        $http.get("/prices/changes", {params: {"startDate": startDateStr, "endDate":endDateStr, "up":$scope.up}})
                .success(function(res) {
                    $scope.size = res.size;
                    $scope.houseItems = res.houseItems;
                    $scope.circle = res.circle;
                    $scope.ajax = true;
                });
    }
}).controller('houseDiff', function($scope, $http, $filter, $modal) {
    $scope.menuType = 'houseDiff';
    var curDate = new Date();
    curDate.setDate(curDate.getDate() - 1);
    $scope.date = curDate;
    $scope.ajax = false;
    $scope.showCircleDetail = function(houseList) {
        var dialogScope = $scope.$new();
        dialogScope.items = houseList;
        var dialog = $modal.open({
            templateUrl:'houseList.html',
            scope:dialogScope,
            windowClass:'modal-full'
        });
    };
    $http.get("/house/diff")
        .success(function(res) {
            $scope.sold = res.sold;
            $scope.disable = res.disable;
            $scope.circle = res.circle;
            $scope.ajax = true;
        });
    $scope.houseDiff = function() {
        $scope.ajax = false;
        $http.get("/house/diff", {params : {"date":$filter('date')($scope.date, 'yyyy-MM-dd')}})
            .success(function(res) {
                $scope.sold = res.sold;
                $scope.disable = res.disable;
                $scope.circle = res.circle;
                $scope.ajax = true;
            });
    };
});