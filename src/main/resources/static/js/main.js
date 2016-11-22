var myApp=angular.module('houseApp', ['ngRoute']);
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
    $scope.endDate = $filter('date')(curDate, 'yyyy-MM-dd');
    curDate.setDate(curDate.getDate() - 1);
    $scope.startDate = $filter('date')(curDate, 'yyyy-MM-dd');
    $scope.ajax = false;
    $scope.up = false;
    $http.get("/prices/changes")
        .success(function(res) {
            $scope.size = res.size;
            $scope.items = res.items;
            $scope.ajax = true;
        });
    $scope.pricesChange = function() {
        $scope.ajax = false;
        $http.get("/prices/changes", {params: {"startDate": $scope.startDate, "endDate":$scope.endDate, "up":$scope.up}})
                .success(function(res) {
                    $scope.size = res.size;
                    $scope.items = res.items;
                    $scope.ajax = true;
                });
    }
});

myApp.controller('pricesTrans', function($scope, $http, $filter) {
    $scope.menuType = 'pricesTrans';
    var curDate = new Date();
    curDate.setDate(curDate.getDate() - 1);
    $scope.date = $filter('date')(date, 'yyyy-MM-dd');
    $scope.ajax = false;
    $http.get("/house/diff")
        .success(function(res) {
            $scope.sold = res.sold;
            $scope.disable = res.disable;
            $scope.ajax = true;
        });
    $scope.houseDiff = function() {
        $scope.ajax = false;
        $http.get("/house/diff", {params : {"date":$scope.date}})
            .success(function(res) {
                $scope.sold = res.sold;
                $scope.disable = res.disable;
                $scope.ajax = true;
            });
    };
});