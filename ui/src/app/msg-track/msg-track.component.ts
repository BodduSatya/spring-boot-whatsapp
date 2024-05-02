import { CommonModule, NgForOf, NgIf, SlicePipe } from '@angular/common';
import { AfterViewInit, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { BsDatepickerModule, BsDatepickerConfig } from 'ngx-bootstrap/datepicker';
import { AppService } from '../app.service';
import { LoaderService } from '../utils/loader/loader.service';
import { ToastService } from '../utils/toast/toast.service';

@Component({
  selector: 'app-msg-track',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    FormsModule,
    SlicePipe,
    RouterLinkActive,
    RouterLink,
    CommonModule,
    BsDatepickerModule
  ],
  templateUrl: './msg-track.component.html',
  styleUrl: './msg-track.component.scss'
})
export class MsgTrackComponent {
  model: any;
  filteredData: any[] = [];
  headers: string[] = [];
  searchText: string = '';
  itemsPerPage: number = 10;
  currentPage: number = 1;
  totalPagesArray: number[] = [];
  itemsPerPageOptions: number[] = [5, 10, 20];
  data: any = [];
  processing: boolean = false;
  datePickerValue: any;
  fromDate: string = '';
  toDate: string = '';
  phoneNumbers: any = '';
  checkAll: any;
  selectedRecords: number = 0;

  constructor(
    private service: AppService,
    private loader: LoaderService,
    private toast: ToastService,
  ) {

  }

  onDateChange(event: any): void {
    console.log('Selected Date:', event);
    this.fromDate = this.formatDate(event[0]);
    this.toDate = this.formatDate(event[1]);
  }

  formatDate(date: Date) {
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0'); // January is 0!
    const year = date.getFullYear();

    return `${day}/${month}/${year}`;
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
    this.filteredData = this.data.filter((item: { [x: string]: { toString: () => string; }; }) => {
      console.log(item);

      for (const key of Object.keys(item)) {
        console.log(key);

        if (item[key]?.toString().toLowerCase().includes(this.searchText.toLowerCase())) {
          return true;
        }
      }
      return false;
    });
    this.calculateTotalPages();
  }

  changePage(page: number) {
    this.currentPage = page;
  }

  getMessages(e: any) {
    if (e)
      e.preventDefault();
    this.checkAll = false;
    this.selectedRecords = 0;
    //console.log(this.datePickerValue,this.fromDate,this.toDate);
    this.loader.setLoading(true);
    if (!this.fromDate || this.fromDate.length === 0 || !this.toDate || this.toDate.length === 0) {
      return this.toast.showWarning("Warning!", 'please select Date Duration.');
    }

    try {
      this.service.getAllMessages(this.fromDate, this.toDate, this.phoneNumbers).subscribe((data: any) => {
        console.log(data);
        this.loader.setLoading(false);
        console.log(data.forEach((x: any) => console.log(x)));

        for (var i = 0; i < data.length; i++) {
          data[i]['checked'] = false
        }
        this.data = data || [];
        this.processing = false;
        this.filteredData = this.data || [];
        // this.data = data || [];
        this.calculateTotalPages();
      })
    } catch (error) {
      console.log(error);
    }
  }

  handleCheckAll(e: any) {
    // console.log(e);
    let mids = [];

    this.data.forEach((x: { [x: string]: any; }) => {
      if (x['sendStatus'] !== '1')
        x['checked'] = e.target.checked

      if (x['checked']) mids.push(x['id']);
    });

    this.selectedRecords = mids.length;

    // this.data = this.filterData;
    this.filterData();
  }

  handleCheck(e: any) {
    let mids = [];
    this.data.forEach((x: { [x: string]: any; }) => {
      if (x['checked']) mids.push(x['id']);
    });
    this.selectedRecords = mids.length;
  }

  resetForm() {
    this.checkAll = false;
    this.data = [];
    this.filteredData = [];
    this.phoneNumbers = '';
    this.fromDate = '';
    this.toDate = '';
    this.selectedRecords = 0;
  }

  confirmMessage(event: any) {
    let mids = [];
    this.data.forEach((x: any) => {
      if (x['checked'])
        mids.push(x['id']);
    });

    // event.preventDefault();
    var confirmation = confirm(`Are you sure you want to retry to send selected ( ${mids.length} ) messages?`);
    if (confirmation) {
      // Send messages to selected customers
      console.log("Messages sent to selected messages");
      //this.sendMessage();
      // console.log('1');

      this.retrySendMessage(event)
    } else {
      // Cancel action
      console.log("Action cancelled");
    }
  }

  retrySendMessage(e: any) {
    e.preventDefault();
    this.loader.setLoading(true);
    let mids: any[] = [];

    this.data.forEach((x: any) => {
      if (x['checked'])
        mids.push(x['id']);
    });

    if (mids.length === 0) {
      this.loader.setLoading(false);
      return this.toast.showWarning("Warning!", 'please select atlease one message to proceed.');
    }

    try {
      this.service.retrySendMessage(mids).subscribe((data: any) => {
        this.loader.setLoading(false);
        console.log(data);
        this.getMessages(null);
        alert(`Total Processed Records : ${data['total_messages']} \nSuccess : ${data['success_count']} \nFailure : ${data['failure_count']}`)
      })
    } catch (error) {
      console.log(error);
    }

  }

}
