# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

### Build UI
* ng build --configuration production

### Package jar
* mvn clean package -DskipTests

POST http://localhost:8086/api/sendMsgV3
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

![Screenshot 2024-05-02 081125](https://github.com/BodduSatya/spring-boot-whatsapp/assets/24984593/f77f1c29-815c-4a07-a01b-9fcd11d6b6ec)

![Screenshot 2024-05-02 081037](https://github.com/BodduSatya/spring-boot-whatsapp/assets/24984593/cac52a43-a378-4950-9501-9dde37319cfd)

![Screenshot 2024-05-02 081053](https://github.com/BodduSatya/spring-boot-whatsapp/assets/24984593/8375bdc1-7d50-4f72-9681-bc1413b0b3c9)

![Screenshot 2024-05-02 081103](https://github.com/BodduSatya/spring-boot-whatsapp/assets/24984593/75604f18-bf30-4723-b32f-88379221be83)

![Screenshot 2024-05-02 081113](https://github.com/BodduSatya/spring-boot-whatsapp/assets/24984593/cf9e4b23-002e-4d87-8d53-cd21af38b43a)



### Build the Docker Image
docker build -t WhatsApp:latest .

### Run the Docker Container
docker run -p 8080:8080 WhatsApp:latest



