import { Component, HostListener, ViewChild } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { LoaderComponent } from "./utils/loader/loader.component";
import { BsDatepickerDirective } from 'ngx-bootstrap/datepicker';
import { NgForOf, NgIf } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet, RouterLink, RouterLinkActive, HttpClientModule,
    LoaderComponent, NgForOf, NgIf
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'ui';

  @ViewChild(BsDatepickerDirective, { static: false })
  datepicker!: BsDatepickerDirective;
  @HostListener('window:scroll')
  onScrollEvent() {
    if (this.datepicker)
      this.datepicker.hide();
  }

  constructor() {
  }

  tabs = [
    { tabName: 'Send Message', routePath: '/message' },
    { tabName: 'Bulk Sender', routePath: '/bulkSender' },
    { tabName: 'Uploads', routePath: '/upload' },
    { tabName: 'Messages Tracker', routePath: '/msgTrack' },
    { tabName: 'About Us', routePath: '/about' }
  ];

  selectedTab:string = this.tabs[this.tabs.length-1].tabName;

  selectTab(tab: any) {
    this.selectedTab = tab.tabName;    
  }

}
