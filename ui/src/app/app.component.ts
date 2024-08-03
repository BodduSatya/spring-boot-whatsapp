import { Component, HostListener, ViewChild } from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { LoaderComponent } from "./utils/loader/loader.component";
import { BsDatepickerDirective } from 'ngx-bootstrap/datepicker';
import { NgForOf, NgIf } from '@angular/common';
import {filter} from "rxjs";

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
  selectedTab:string = '';
  constructor(private router: Router, private activatedRoute: ActivatedRoute) {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.updateSelectedTab();
    });
    // Set initial tab
    this.updateSelectedTab();
  }

  updateSelectedTab() {
    const currentRoute = this.router.url;
    const matchingTab = this.tabs.find(tab => tab.routePath === currentRoute);
    if (matchingTab) {
      this.selectedTab = matchingTab.tabName;
    }
  }

  tabs = [
    { tabName: 'Send Message', routePath: '/message' },
    { tabName: 'Bulk Sender', routePath: '/bulkSender' },
    { tabName: 'Uploads', routePath: '/upload' },
    { tabName: 'Messages Tracker', routePath: '/msgTrack' },
    { tabName: 'About Us', routePath: '/about' }
  ];

  selectTab(tab: any) {
    this.selectedTab = tab.tabName;
  }

  logout() {
    // Add your logout logic here (e.g., remove tokens, call API, etc.)
    // this.router.navigate(['/logout']).then(r => {
    //
    // });
    window.location.href='/logout';
  }

}
