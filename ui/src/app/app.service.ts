import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AppService {

  constructor(private http: HttpClient) { }
  // baseUrl: string = environment.apiUrl
  baseUrl: string = '';

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

  getAllMessages(fromDate: string, toDate: string, mobileNo: string,msgStatus:string) {
    return this.http.get(`${this.baseUrl}/stats/getAllMessages?fromDate=${fromDate}&toDate=${toDate}&mobileNo=${mobileNo}&msgStatus=${msgStatus}`);
  }

  retrySendMessage(mids: any) {
    return this.http.post(`${this.baseUrl}/stats/sendQueuedMessagesByMID`, mids);
  }

  deleteMessage(mids: any) {
    const options = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      body: mids
    };
    // console.log('mids',mids)
    return this.http.delete(`${this.baseUrl}/stats/deleteMessagesByMID`, options);
  }

}
