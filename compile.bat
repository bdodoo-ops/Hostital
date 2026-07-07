@echo off
setlocal

set JAVAC=C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot\bin\javac.exe

echo ============================================
echo  Hospital Management System - Compiling...
echo ============================================

if not exist bin mkdir bin
if not exist data mkdir data

"%JAVAC%" -encoding UTF-8 -d bin ^
    src\MainApp.java ^
    src\base\Entity.java ^
    src\base\Displayable.java ^
    src\base\Manager.java ^
    src\util\FileUtil.java ^
    src\util\AppTheme.java ^
    src\util\Validator.java ^
    src\model\QueuePatient.java ^
    src\model\RecPatient.java ^
    src\model\RecDoctor.java ^
    src\model\MedRecord.java ^
    src\model\Medicine.java ^
    src\model\Sale.java ^
    src\model\Appointment.java ^
    src\model\Donor.java ^
    src\model\BloodRequest.java ^
    src\model\Vaccine.java ^
    src\model\VaccRecord.java ^
    src\model\AmbulanceVehicle.java ^
    src\model\AmbDriver.java ^
    src\model\Dispatch.java ^
    src\model\LabTest.java ^
    src\model\TestOrder.java ^
    src\model\TestResult.java ^
    src\model\TestConstituent.java ^
    src\model\BillService.java ^
    src\model\Bill.java ^
    src\model\Payment.java ^
    src\model\Mother.java ^
    src\model\HealthRecord.java ^
    src\manager\QueueManager.java ^
    src\manager\RecordManager.java ^
    src\manager\PharmacyManager.java ^
    src\manager\AppointmentManager.java ^
    src\manager\BloodManager.java ^
    src\manager\VaccinationManager.java ^
    src\manager\AmbulanceManager.java ^
    src\manager\LabManager.java ^
    src\manager\BillingManager.java ^
    src\manager\MaternalManager.java ^
    src\ui\BasePanel.java ^
    src\ui\FormDialog.java ^
    src\ui\MainWindow.java ^
    src\panel\QueuePanel.java ^
    src\panel\RecordsPanel.java ^
    src\panel\PharmacyPanel.java ^
    src\panel\AppointmentPanel.java ^
    src\panel\BloodPanel.java ^
    src\panel\VaccinationPanel.java ^
    src\panel\AmbulancePanel.java ^
    src\panel\LabPanel.java ^
    src\panel\BillingPanel.java ^
    src\panel\MaternalPanel.java ^
    src\panel\PatientsPanel.java ^
    src\panel\DoctorsPanel.java

if errorlevel 1 (
    echo.
    echo COMPILATION FAILED. See errors above.
    pause
    exit /b 1
)

echo.
echo Compilation successful! Run run.bat to launch.
pause
