import { Routes } from '@angular/router';
import {SendMsgFormComponent} from "./send-msg-form/send-msg-form.component";
import {BulkSenderComponent} from "./bulk-sender/bulk-sender.component";
import {UploadComponent} from "./upload/upload.component";
import {AboutusComponent} from "./aboutus/aboutus.component";
import { MsgTrackComponent } from './msg-track/msg-track.component';
import {LoginComponent} from "./login/login.component";

export const routes: Routes = [
  // { path: 'login', component: LoginComponent },
  // { path: '', redirectTo: '/login', pathMatch: 'full' }, // Adjust as necessary
  {path:'message', component:SendMsgFormComponent},
  {path:'bulkSender', component:BulkSenderComponent},
  {path:'upload', component:UploadComponent},
  {path:'msgTrack', component:MsgTrackComponent},
  {path:'about', component:AboutusComponent},
  {path:'',redirectTo:'/about',pathMatch:'full'},
];
