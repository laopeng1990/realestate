houseApp.factory('priceChange', ['$http', function($http) {
    return {
        getPriceChange: function(startDate, endDate, up) {
            $http.get("/prices/changes", {params: {"startDate": $scope.startDate, "endDate":$scope.endDate}})
                    .success(function(res) {
                        return res;
                    });
        }
    };
}]);