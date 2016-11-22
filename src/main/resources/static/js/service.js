houseApp.factory('priceChange', ['$http', function($http) {
    return {
        getPriceChange: function(startDate, endDate, up) {
            $http.get("/prices/changes", {params: {"startDate": startDate, "endDate":endDate}})
                    .success(function(res) {
                        return res;
                    });
        }
    };
}]);

houseApp.factory('houseDiff', ['$http', function($http) {
    return {
        getHouseDiff: function(date) {
            $http.get("house/diff", {params : {"date": date}})
                    .success(function(res) {
                        return res;
                    });
        }
    };
}])