import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AppService {

  constructor(private http: HttpClient) { }
  baseUrl: string = environment.apiUrl

  sendMessage(data: any) {
    return this.http.post(`${this.baseUrl}/api/sendMsgV3`, data);
  }

  singleFileUpload(formData: FormData) {
    return this.http.post(`${this.baseUrl}/uploadFile`, formData);
  }

  uploadMultipleFiles(formData: FormData) {
    return this.http.post(`${this.baseUrl}/uploadMultipleFiles`, formData);
  }

  loadFiles() {
    return this.http.get(`${this.baseUrl}/loadUploadFiles`);
  }

  sendMessageV1(data: any) {
    return this.http.post(`${this.baseUrl}/api/sendMsg`, data);
  }

  getAllMessages(fromDate: string, toDate: string, mobileNo: string) {
    return this.http.get(`${this.baseUrl}/stats/getAllMessages?fromDate=${fromDate}&toDate=${toDate}&mobileNo=${mobileNo}`);
  }

  retrySendMessage(mids: any) {
    return this.http.post(`${this.baseUrl}/stats/sendQueuedMessagesByMID`, mids);
  }

}
