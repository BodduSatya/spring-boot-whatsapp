import {Injectable, NgZone} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class SseService {

  constructor(private http: HttpClient) { }

  getServerSentEvents(url: string): Observable<MessageEvent> {
    return new Observable<MessageEvent>((observer) => {
      const eventSource = new EventSource(url);
      eventSource.onmessage = (event: MessageEvent) => {
        observer.next(event);
      };
      eventSource.onerror = (error) => {
        observer.error(error);
      };
    });
  }


}

export interface MessageData {
  status: string;
  message: string;
  id:string;
  event:string;
  data:string;
}
