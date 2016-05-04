/**
 * Created by dima on 30.04.16.
 */
'use strict';

function staffManagementService(http) {

    var service = {};
    
    service.showAllEmployees = function () {
        return http.post('/admin/showAllEmployee').then(function (response) {
            return response.data;
        });
    };

    service.addEmployee = function (firstName, secondName, lastName, email, roles) {
        http({
            method : 'POST',
            url : '/admin/addEmployee',
            contentType: 'application/json',
            data : JSON.stringify({
                firstName: firstName,
                secondName: secondName,
                lastName: lastName,
                email: email,
                roleList: roles
            })
        })
    };


    service.editEmployee = function (id,firstName, secondName, lastName, email, roles) {
        http({
            method : 'POST',
            url : '/admin/editEmployee',
            contentType: 'application/json',
            data : {
                id:id,
                firstName: firstName,
                secondName: secondName,
                lastName: lastName,
                email: email,
                roleList: roles
            }
        });
    };

    service.changeEmployeeStatus = function (email) {
        console.log(email)
        http({
            method : 'GET',
            url : '/admin/changeEmployeeStatus',
            params : {email:email}
        }).success(function (data, status, headers) {

            console.log(data);
            if (status === 409){
                // TODO show message user already exist!!!
            }
        }).error(function (data, status, headers) {
            console.log(status);
        });
    };


    service.showAssigned = function (email) {
        http({
            method : 'POST',
            url : '/admin/showAssignedStudent',
            params : {email:email}
        }).success(function (data, status, headers) {
            console.log(data);
        }).error(function (data, status, headers) {
            console.log(status);
        });
    };
    
    return service;
}



angular.module('appStaffManagement')
    .service('staffManagementService', ['$http', staffManagementService]);