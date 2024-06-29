import {Component, ElementRef, ViewChild} from '@angular/core';
import {FormsModule} from "@angular/forms";
import {NgForOf, NgIf} from "@angular/common";
import {AppService} from "../app.service";
import {LoaderService} from "../utils/loader/loader.service";
import {ToastService} from "../utils/toast/toast.service";

@Component({
  selector: 'app-send-msg-form',
  standalone: true,
  imports: [
    FormsModule, NgIf, NgForOf
  ],
  templateUrl: './send-msg-form.component.html',
  styleUrl: './send-msg-form.component.scss'
})
export class SendMsgFormComponent {
  errMsg='';
  typeofmsg='1';
  phoneNumbers='';
  message='';
  mediaUrl:string[] = [];
  caption='';
  @ViewChild('fileInput_el') fileInput_el: null | undefined;
  fileInput: File | null = null;
  sendMsgBtn: boolean = true;
  fileName: string[] =[];

  constructor(private service:AppService,
              private loader: LoaderService,
              private toast: ToastService  ) { }

  fileExt = [
    {
      name:'image', ext:['.jpg','.jpeg','.png','.gif']
    },{
      name:'video', ext:['.mpeg','.wav','.mp4']
    },{
      name:'audio', ext:['.mp3','.wav']
    },{
      name:'document', ext:['.txt','.doc','.docx','.ppt','.pptx','.xls','.xlsx','.pdf']
    },{
      name:'gif', ext:['.mpeg','.wav','.mp4']
    }];

  msgTypes=[
    {
      label:'Text', value:'1'
    },{
      label:'Image', value:'2'
    },{
      label:'Document', value:'3'
    },{
      label:'Video', value:'4'
    },{
      label:'Audio', value:'5'
    },{
      label:'GIF', value:'6'
    }];

  fileExtensions (){
    let msgType:any = this.msgTypes.filter(x=>x['value']===this.typeofmsg);
    let ext = this.fileExt.filter(x=>x['name']=== msgType[0]['label'].toLowerCase());
    return ext ? ext[0]['ext'] :'*';
  }

  onFileSelected(event: any) {
    this.mediaUrl = [];
    this.fileName = [];
    this.fileInput = event.target.files[0] as File;

    if ( !event.target.files || event.target.files.length === 0 ) {
      this.toast.showWarning('Warning!','No file selected')
      return;
    }

    this.loader.setLoading(true);
    // const formData = new FormData();
    // formData.append('file', this.fileInput);
    // formData.append('file', event.target.files);

    var formData = new FormData();
    for( var index = 0; index < event.target.files.length; index++ ) {
      formData.append("files", event.target.files[index]);
    }

    this.service.uploadMultipleFiles(formData).subscribe(
      (response:any) => {
        this.loader.setLoading(false);
        this.fileInput = null;
        console.log('Upload response:', response);
        if( response ) {
          if (response.length > 1) {
            var success = 0;
            var fail = 0;
            for (var i = 0; i < response.length; i++) {
              if (response[i].fileName && response[i].fileName.length > 0) {
                success = success + 1;
                this.mediaUrl.push(response[i].fileDownloadUri);
                this.fileName.push(response[i].fileName);
              } else {
                fail = fail + 1;
              }
            }
            if( success >= 1 )
              this.toast.showSuccess('Success!', success+' / '+response.length +' files uploaded successfully.');
            else
              this.toast.showError('Error!', fail+' / '+ response.length +' files upload failed.');
          }
          else if (response[0]['fileDownloadUri'].length > 0) {
            this.mediaUrl.push(response[0].fileDownloadUri);
            this.fileName.push(response[0].fileName);
            this.toast.showSuccess('Success!', response[0]['message']);
          } else {
            this.toast.showError('Error!', response[0]['message']);
          }
        }
      },
      (error: any) => {
        console.error('Upload failed:', error);
        this.loader.setLoading(false);
        this.toast.showError('Error!','Upload failed!');
      }
    );
  }

  resetForm(){
    if(this.fileInput_el) {
      // @ts-ignore
      this.fileInput_el.nativeElement.value = '';
    }
    this.errMsg='';
    this.typeofmsg='1';
    this.phoneNumbers='';
    this.message='';
    this.caption='';
    this.sendMsgBtn = true;
    this.mediaUrl=[];
    this.fileName =[];
  }

  sendMessage() {
    let msgType:any = this.msgTypes.find(x=>x['value']===this.typeofmsg);
    msgType = msgType ? msgType['label'].toLowerCase() : 'text';
    if( !this.phoneNumbers || this.phoneNumbers.trim().length === 0 ){
      this.toast.showWarning("Warning!","Please enter mobile number");
      return;
    }
    else if( msgType ==='text' && ( !this.message || this.message.trim().length ==0 ) ){
      this.toast.showWarning("Warning!","Please select message type");
      return;
    }
    else if( msgType !=='text' && ( this.mediaUrl == null || this.mediaUrl.length ===0 )){
      this.toast.showWarning("Warning!","Please upload media.");
      return;
    }
    this.loader.setLoading(true);
    this.sendMsgBtn = false;
    let body={
      "toMobileNumber":this.phoneNumbers,
      "typeOfMsg":msgType,
      "message":this.message,
      "mediaUrl2":this.mediaUrl,
      "caption":this.caption,
      "fileName2":this.fileName
    }

    this.service.sendMessage(body).subscribe((data:any) => {
      console.log(data);
      this.sendMsgBtn = true;
      this.loader.setLoading(false);
      if( data ){
        if( data['status'].toLowerCase()==='ok'){
          this.resetForm();
          this.toast.showSuccess("Success!",data['message']);
        }
        else
          this.toast.showError("Failed!",data['message']);
      }
    },error => {
      this.loader.setLoading(false);
      this.toast.showError("Failed!",'Something went wrong!');
      console.log('Error!',error);
    });
  }

}
