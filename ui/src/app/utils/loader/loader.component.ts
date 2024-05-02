import {Component, Input} from '@angular/core';
import {AsyncPipe, NgIf} from "@angular/common";
import {LoaderService} from "./loader.service";

@Component({
  selector: 'app-loader',
  standalone: true,
  imports: [
    NgIf,
    AsyncPipe
  ],
  templateUrl: './loader.component.html',
  styleUrl: './loader.component.scss'
})
export class LoaderComponent {
  constructor(public loader: LoaderService) { }
}
