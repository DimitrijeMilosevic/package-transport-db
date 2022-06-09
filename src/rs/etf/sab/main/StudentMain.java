package rs.etf.sab.main;

import rs.etf.sab.interfaces.*;
import rs.etf.sab.operations.*;
import rs.etf.sab.tests.*;


public class StudentMain {
	

    public static void main(String[] args) {
        CityOperations cityOperations = new MyCityOperations();
        DistrictOperations districtOperations = new MyDistrictOperations();
        CourierOperations courierOperations = new MyCourierOperations();
        CourierRequestOperation courierRequestOperation = new MyCourierRequestOperation();
        GeneralOperations generalOperations = new MyGeneralOperations();
        UserOperations userOperations = new MyUserOperations();
        VehicleOperations vehicleOperations = new MyVehicleOperations();
        PackageOperations packageOperations = new MyPackageOperations();
        TestHandler.createInstance(
                cityOperations,
                courierOperations,
                courierRequestOperation,
                districtOperations,
                generalOperations,
                userOperations,
                vehicleOperations,
                packageOperations);
        TestRunner.runTests();
    }
    
}
