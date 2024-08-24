# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

### Build UI
* ng build --configuration production

### Package jar
* mvn clean package -DskipTests

POST http://localhost/api/sendMsgV3
# Individual Contact
{
    "toMobileNumber":"",
    "typeOfMsg":"",
    "message":"",
    "mediaUrl2":"",
    "caption":""
}

# Group message :
{
    "toMobileNumber":"xxxxxxxxxxxxxx@g.us",
    "typeOfMsg":"text",
    "message":"Hi",
    "mediaUrl2":"",
    "caption":"hello",
    "groupMsg":true
}

![image](https://github.com/user-attachments/assets/a28d3d63-12f5-47f1-ba2c-96ae54d7e416)

![image](https://github.com/user-attachments/assets/538bd7ad-7846-424d-b6f6-2bf5cfdf33d0)

![image](https://github.com/user-attachments/assets/160edf36-4821-48bb-bc31-13b46e117427)

![image](https://github.com/user-attachments/assets/caf3407c-1832-4a11-aa7a-c57b7910a4cc)

![image](https://github.com/user-attachments/assets/80832772-cc68-413e-947d-b326ccba9257)

![image](https://github.com/user-attachments/assets/45153f2b-847c-49c0-9e79-fd605d76fb1f)

### Build the Docker Image
docker build -t WhatsApp:latest .

### Run the Docker Container
docker run -p 8080:8080 WhatsApp:latest

### h2 commands
SHOW COLUMNS FROM MESSAGES;

### API Docs
http://localhost:8086/swagger-ui.html
