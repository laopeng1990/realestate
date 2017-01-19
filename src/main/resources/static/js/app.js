var houseApp = angular.module('houseApp', ['ngRoute','ui.bootstrap']);
houseApp.config(function($routeProvider) {
            $routeProvider.
            when('/prices', {
                templateUrl: '/prices.html',
                title: '价格变动',
                controller: 'priceChangeCtrl'
            }).
            when('/prices/trans', {
                templateUrl: '/trans.html',
                title: '成交查询',
                controller: 'transCtrl'
            }).
            otherwise({
                redirectTo:'/'
            });
        });