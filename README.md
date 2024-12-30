# Intergrated-IoT-platforms
Interoperability IoT Architecture based on EdgeX Gateway with Distributed Rules Engine for Trigger-Action Programming Service
## Architecture
* [x] oneM2M nCube device (Raspberry Pi 4)
* [x] OCF IoTivity device (Raspberry Pi 4)
* [x] oneM2M Mobius server (PC Desktop)
* [x] OCF IoTivity server (PC Desktop)
* [ ] EdgeX gateway ()
* [ ] Web application
* [ ] Rules Registration
      
(All code and paper will be published when the project and fund are finished.)
## Installation
1. [Download and setup Mobius platform](https://github.com/IoTKETI/Mobius/wiki/Mobius_v2.0.0_EN_Linux)
2. [Download and setup IoTivity platform](https://iotivity.org/build_linux/)
3. [Download and setup EdgeX platform](https://docs.edgexfoundry.org/3.1/getting-started/quick-start/)

## How to run
Change the address inside the code
### Run the nCube device on Raspberry Pi 4
1. Open `ncube_device` folder and build project in terminal:
   `./build_ncube_device.sh`
2. Run the nCube device:
   `./run_ncube_device.sh`

### Run the OCF device on Raspberry Pi 4
1. Open `ocf_device` folder and build project in terminal:
   `./build_ocf_device.sh`
2. Run the OCF device:
   `./run_ocf_device.sh`

### Run the oneM2M and OCF servers on PC Desktop
You can run them by some Java IDEs like Spring Tool Suite 4
![](https://github.com/anhtuan85/Intergrated-IoT-platforms/blob/main/imgs/Spring-Tool-Suite.jpg)
