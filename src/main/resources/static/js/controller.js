houseApp.controller('priceChangeCtrl', function($scope, priceChange) {
    $scope.menuType = 'prices';
    var curDate = new Date();
    curDate.setDate(curDate.getDate() - 1);
    $scope.endDate = $filter('date')(curDate, 'yyyy-MM-dd');
    curDate.setDate(curDate.getDate() - 1);
    $scope.startDate = $filter('date')(curDate, 'yyyy-MM-dd');
    $scope.pricesSuccess = false;
    var resData = priceChange.getPriceChange(startDate, endDate, );
});