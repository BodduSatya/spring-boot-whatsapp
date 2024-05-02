 import { Component } from '@angular/core';
 import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [
    NgForOf
  ],
  templateUrl: './toast.component.html',
  styleUrl: './toast.component.scss'
})
export class ToastComponent {
  constructor( ) { }

}
