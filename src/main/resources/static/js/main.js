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
    when('/prices/trans', {
        controller: 'pricesTrans',
        templateUrl: '/trans.html',
        title: '成交查询',
        pageSlug: 'pricesTrans'
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
    $scope.pricesSuccess = false;
    $scope.up = false;
    $http.get("/prices/changes")
        .success(function(res) {
            $scope.size = res.size;
            $scope.items = res.items;
            $scope.pricesSuccess = true;
        });
    $scope.pricesChange = function() {
        $scope.pricesSuccess = false;
        $http.get("/prices/changes", {params: {"startDate": $scope.startDate, "endDate":$scope.endDate, "up":$scope.up}})
                .success(function(res) {
                    $scope.size = res.size;
                    $scope.items = res.items;
                    $scope.pricesSuccess = true;
                });
    }
});

myApp.controller('pricesTrans', function($scope) {
    $scope.menuType = 'pricesTrans';
});