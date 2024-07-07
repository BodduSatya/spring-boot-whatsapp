import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import 'bootstrap/dist/js/bootstrap.esm.js';
import 'bootstrap/dist/css/bootstrap.min.css';

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));
