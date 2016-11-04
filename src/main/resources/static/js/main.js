var myApp=angular.module('houseApp', ['ngRoute']);
myApp.filter("housePrice", function() {
    return function(input) {
        return input / 10000 + "万";
    };
}).filter("unitPrice", function() {
    return function(input) {
        return input + "元";
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

myApp.controller('pricesChange', function($scope, $http) {
    $scope.menuType = 'prices';
    $http.get("/prices/changes")
        .success(function(res) {
            $scope.size = res.size;
            $scope.items = res.items;
        });
});

myApp.controller('pricesTrans', function($scope) {
    $scope.menuType = 'pricesTrans';
});