var myApp=angular.module('houseApp', ['ngRoute']);
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
            $scope.items = res.items;
        });
});

myApp.controller('pricesTrans', function($scope) {
    $scope.menuType = 'pricesTrans';
});