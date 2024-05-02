import { Component, ElementRef, NgZone, ViewChild } from '@angular/core';
import * as XLSX from 'xlsx';
import { NgForOf, NgIf, SlicePipe } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { RouterLink, RouterLinkActive } from "@angular/router";
import { AppService } from "../app.service";
import { LoaderService } from "../utils/loader/loader.service";
import { ToastService } from "../utils/toast/toast.service";
import { Observable, Observer } from "rxjs";
import { CommonModule } from '@angular/common';
import { environment } from '../../environments/environment';

type AOA = any[][];

@Component({
  selector: 'app-bulk-sender',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    FormsModule,
    SlicePipe,
    RouterLinkActive,
    RouterLink,
    CommonModule
  ],
  templateUrl: './bulk-sender.component.html',
  styleUrl: './bulk-sender.component.scss'
})
export class BulkSenderComponent {
  data: AOA = [];
  fileName: string = '';
  sheetName: string = '';

  filteredData: any[] = [];
  headers: string[] = [];
  searchText: string = '';
  itemsPerPage: number = 10;
  currentPage: number = 1;
  totalPagesArray: number[] = [];
  itemsPerPageOptions: number[] = [5, 10, 20];
  @ViewChild('fileInput_el') fileInput_el: ElementRef | undefined;
  mediaUrl: string = '';
  sendMsgBtn: boolean = true;
  message: string = '';
  caption: string = '';
  phoneNumbers: any = '';
  btnStatus: boolean = true;

  processing: boolean = false;
  displayString: any = '';
  success = 0;
  failed = 0;
  serverfileName: any;
  progressValue: number = 0;

  constructor(private service: AppService,
    private loader: LoaderService,
    private toast: ToastService,
    // private sseService: SseService,
    private ngZone: NgZone
  ) {

    this.success = 0;
    this.failed = 0;
  }

  onFileChange(evt: any) {
    const target: DataTransfer = <DataTransfer>(evt.target);
    if (target.files.length !== 1) throw new Error('Cannot use multiple files');

    const file: File = target.files[0];
    this.fileName = file.name; // Capture the file name

    const reader: FileReader = new FileReader();
    reader.onload = (e: any) => {
      const bstr: string = e.target.result;
      const wb: XLSX.WorkBook = XLSX.read(bstr, { type: 'binary' });
      const wsname: string = wb.SheetNames[0];

      this.sheetName = wb.SheetNames[0];

      const ws: XLSX.WorkSheet = wb.Sheets[wsname];
      this.data = <AOA>(XLSX.utils.sheet_to_json(ws, { header: 1 }));

      if (this.data[0][0] === 'toMobileNumber') {
        this.filteredData = this.data.slice(1); // Exclude header row
        this.headers = this.data[0];
        this.calculateTotalPages();

        this.uploadSelectedFile(file);
      }
      else {
        alert('Uploaded xls data is not in valid template.')
        // this.toastService.showToast('Error', 'Uploaded xls data is not in valid template.',"");
        this.data = [];
        this.sheetName = '';
        this.fileName = '';
        // @ts-ignore
        this.fileInput.nativeElement.value = '';
      }
    };
    reader.readAsBinaryString(target.files[0]);
  }

  changeItemsPerPage() {
    this.calculateTotalPages();
  }

  calculateTotalPages() {
    const totalPages = Math.ceil(this.filteredData.length / this.itemsPerPage);
    this.totalPagesArray = Array.from({ length: totalPages }, (_, i) => i + 1);
  }

  filterData() {
    this.currentPage = 1;
    this.filteredData = this.data.slice(1).filter((row) =>
      row.some((cell) =>
        cell.toString().toLowerCase().includes(this.searchText.toLowerCase())
      )
    );
    this.calculateTotalPages();
  }

  changePage(page: number) {
    this.currentPage = page;
  }

  exportAsExcel() {
    const data = [
      ['toMobileNumber', 'typeOfMsg', 'message', 'mediaUrl', 'caption'],
      ['919898989898', 'text', 'sample message text', '', ''],
      ['919797979797', 'image', '', 'https://www.pixelstalk.net/wp-content/uploads/2016/08/Best-Nature-Full-HD-Images-For-Desktop.jpg', 'sample caption']
    ];

    // Create a new workbook and a worksheet
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    const ws: XLSX.WorkSheet = XLSX.utils.aoa_to_sheet(data);

    // Add the worksheet to the workbook
    XLSX.utils.book_append_sheet(wb, ws, 'Sheet1');

    // Generate a buffer
    const wbout: ArrayBuffer = XLSX.write(wb, { bookType: 'xlsx', type: 'array' });

    // Create a Blob from the buffer
    let blob = new Blob([wbout], { type: 'application/octet-stream' });

    // Create an object URL and link
    let url = window.URL.createObjectURL(blob);
    let anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = 'whatapp_sample_data_template.xlsx';

    // Append anchor to body, click it, and remove it
    document.body.appendChild(anchor);
    anchor.click();
    document.body.removeChild(anchor);

    // Optionally, release the object URL
    window.URL.revokeObjectURL(url);
  }

  resetForm() {
    this.phoneNumbers = '';
    this.message = '';
    this.caption = '';
    this.mediaUrl = '';
    this.sendMsgBtn = true;
    this.fileName = '';
    this.sheetName = '';
    this.data = [];
    this.filteredData = [];
    if (this.fileInput_el) {
      // @ts-ignore
      this.fileInput_el.nativeElement.value = '';
    }
    this.btnStatus = true;
  }

  sendMessage() {
    this.btnStatus = false;
    let toMobileNumber = "";
    let typeOfMsg = "";
    let message = "";
    let mediaUrl = "";
    let caption = "";
    let fileName = "";

    this.processing = true;


    for (var i = 1; i < this.data.length; i++) {

      toMobileNumber = "";
      typeOfMsg = "";
      message = "";
      mediaUrl = "";
      caption = "";
      fileName = "";

      // console.log(this.data[i]);

      for (var j = 0; j < this.data[i].length; j++) {
        // console.log(this.headers[j]);
        this.processing = true;
        if (this.headers[j].toLowerCase().trim() === 'tomobilenumber') {
          toMobileNumber = this.data[i][j] ? this.data[i][j].trim() : '';
        }
        else if (this.headers[j].toLowerCase().trim() === 'typeofmsg') {
          typeOfMsg = this.data[i][j] ? this.data[i][j].trim() : '';
        }
        else if (this.headers[j].toLowerCase().trim() === 'message') {
          message = this.data[i][j] ? this.data[i][j].trim() : '';
        }
        else if (this.headers[j].toLowerCase().trim() === 'mediaurl') {
          mediaUrl = this.data[i][j] ? this.data[i][j].trim() : '';
        }
        else if (this.headers[j].toLowerCase().trim() === 'caption') {
          caption = this.data[i][j] ? this.data[i][j].trim() : '';
        }
      }

      // console.log(toMobileNumber, typeOfMsg, message, mediaUrl);

      if (!toMobileNumber || toMobileNumber.trim().length === 0) {
        continue;
      } else if (typeOfMsg === 'text' && (!message || message.trim().length == 0)) {
        continue;
      } else if (typeOfMsg !== 'text' && (mediaUrl == null || mediaUrl.length === 0)) {
        continue;
      }
      // this.loader.setLoading(true);
      this.sendMsgBtn = false;
      let body = {
        "toMobileNumber": toMobileNumber,
        "typeOfMsg": typeOfMsg,
        "message": message,
        "mediaUrl": mediaUrl,
        "caption": caption,
        "fileName": mediaUrl ? fileName : mediaUrl.substring(mediaUrl.lastIndexOf('/') + 1)
      }
      console.log(body);
      this.processing = true;
      setTimeout(() => {
        // @ts-ignore
        this.sendMessageRequest(body, i);
      }, i * 5000); // Multiply index by delay time (e.g., 1000 milliseconds) to introduce delay

    }
    //alert(this.displayString)
    this.sendMsgBtn = true;
  }

  sendMessageRequest(body: any, i: number) {
    try {
      this.service.sendMessageV1(body).subscribe((data: any) => {
        console.log(data);
        // this.loader.setLoading(false);
        this.processing = false;
        if (data) {
          if (data['status'].toLowerCase() === 'ok') {
            this.success = this.success + 1;
            // this.resetForm();
            // this.toast.showSuccess("Success!", data['message']);
            this.displayString = (i - 1) + ' / ' + (this.data.length - 1);
          } else {
            this.failed = this.failed + 1;
            // this.toast.showError("Failed!", data['message']);
            this.displayString = (i - 1) + ' / ' + (this.data.length - 1);
          }
        }
      }, (error: any) => {
        this.failed = this.failed + 1;
        this.displayString = (i - 1) + ' / ' + (this.data.length - 1);
        this.processing = false;
        this.toast.showError("Failed!", 'Something went wrong!');
        console.log('Error!', error);
      });
    } catch (e) {
      console.log(e);
    }
  }

  confirmMessage(event: any) {
    // event.preventDefault();
    var confirmation = confirm("Are you sure you want to send messages to the selected customers?");
    if (confirmation) {
      // Send messages to selected customers
      console.log("Messages sent to selected customers");
      //this.sendMessage();
      // console.log('1');

      this.search(event)
    } else {
      // Cancel action
      console.log("Action cancelled");
    }
  }

  endSearch: boolean = false;
  showProgressBar: boolean = false;
  results: string[] = [];
  resultsObserver: Observable<string[]> | undefined;
  apiUrl: string = environment.apiUrl + '/server-events/sendMsgV4';
  // apiUrl: string = 'http://localhost:8086/api/stream';

  search(event: any) {
    // event.preventDefault();
    // console.log(2);
    this.btnStatus = false;
    this.processing = true;
    this.endSearch = false;
    this.results = [];
    this.createEventSourceObserver();
  }

  createEventSourceObserver(): any {
    console.log('createEventSourceObserver');

    this.resultsObserver = new Observable<string[]>((observer: Observer<string[]>) => {
      let source: EventSource | null = new EventSource(this.apiUrl + "?fileName=" + this.serverfileName);

      source.onmessage = (event) => {
        console.log(event.data);
        this.results.push(event.data);
        // this.results.sort();
        this.ngZone.run(() => {
          observer.next(this.results);
        });
        document.documentElement.scrollTop = 9999999999;

        try {
          console.log(111111);
          let j = JSON.parse(event.data);
          if (j.status && j['status'].toUpperCase() === 'OK') {
            this.success = this.success + 1;
            console.log(this.success);
          }
          else
            this.failed = this.failed + 1;

          this.progressValue = Math.round((this.results.length / this.data.length ) * 100);

        } catch (e) {
          console.log(e);
        }
      };

      // source.onopen = (event) => {
      //   if (this.endSearch) {
      //     source?.close();
      //     this.ngZone.run(() => {
      //       observer.complete();
      //       this.processing = false;
      //     });
      //   }
      //   this.endSearch = true;
      // };

      source.onerror = (error) => {
        console.error('EventSource error:', error);
        // if (source) {
        source?.close();
        source = null;
        this.processing = false;
        alert(`Total Processed Records : ${this.results.length} \nSuccess : ${this.success} \nFailure : ${this.failed}`)
        // }
      };

      // source.close = () => {
      //   console.log('EventSource closed');
      //   // You can add reconnection logic here if needed
      // };

      return () => {
        // if (source) {
        source?.close();
        source = null;
        // }
      };
    });
  }

  uploadSelectedFile(file: File) {
    var formData = new FormData();
    formData.append("files", file);

    this.service.uploadMultipleFiles(formData).subscribe(
      (response: any) => {
        if (response) {
          if (response[0]['fileDownloadUri'].length > 0) {
            this.serverfileName = response[0].fileName;
            this.toast.showSuccess('Success!', 'Document data extracted successfully!');
          } else {
            this.toast.showError('Error!', 'something went wrong while parsing the file.');
          }
        }
      },
      (error: any) => {
        console.error('Upload failed:', error);
        this.loader.setLoading(false);
        this.toast.showError('Error!', 'Upload failed!');
      }
    );
  }

}
