import {Component, ElementRef, ViewChild,OnInit } from '@angular/core';
import {NgForOf, NgIf, SlicePipe} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {RouterLink, RouterLinkActive} from "@angular/router";
import {AppService} from "../app.service";
import {LoaderService} from "../utils/loader/loader.service";
import {ToastService} from "../utils/toast/toast.service";

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    FormsModule,
    SlicePipe,
    RouterLinkActive,
    RouterLink
  ],
  templateUrl: './upload.component.html',
  styleUrl: './upload.component.scss'
})
export class UploadComponent {
  data: any = [];
  fileName: string = '';
  sheetName: string='';

  filteredData: any[]=[];
  headers: string[]=[];
  searchText: string = '';
  itemsPerPage: number = 10;
  currentPage: number = 1;
  totalPagesArray: number[]=[];
  itemsPerPageOptions: number[] = [5, 10, 20];
  @ViewChild('fileInput_el') fileInput_el: ElementRef | undefined;
  message: string='';

  constructor(private service:AppService,
              private loader: LoaderService,
              private toast: ToastService  ) { }

  ngOnInit(): void {
    this.loadFiles();
  }

  fileExtensions (){
    ['.jpg','.jpeg','.png','.gif','.mpeg','.wav','.mp4',
      '.mp3','.wav','.txt','.doc','.docx','.ppt','.pptx','.xls',
      '.xlsx','.pdf','.mpeg','.wav','.mp4']
  }

  loadFiles(){
    this.service.loadFiles().subscribe((data:any) => {
      // console.log(data);
      this.loader.setLoading(false);
      if( data ){
        this.data = data||[];
        this.filteredData = data||[];
        this.calculateTotalPages();
      }
    },error => {
      this.loader.setLoading(false);
      // this.toast.showError("Failed!",'Something went wrong!');
      console.log('Error!',error);
    });
  }

  onFileSelected(event: any) {

    if ( !event.target.files || event.target.files.length === 0 ) {
      this.toast.showWarning('Warning!','No file selected')
      return;
    }

    this.loader.setLoading(true);

    var formData = new FormData();
    for( var index = 0; index < event.target.files.length; index++ ) {
      formData.append("files", event.target.files[index]);
    }

    this.service.uploadMultipleFiles(formData).subscribe(
      (response:any) => {
        this.loader.setLoading(false);
        // this.fileInput = null;
        console.log('Upload response:', response);
        if( response ) {
          if (response.length > 1) {
            var success = 0;
            var fail = 0;
            for (var i = 0; i < response.length; i++) {
              if (response[i].fileName && response[i].fileName.length > 0) {
                success = success + 1;
                this.resetForm();
                this.loadFiles();
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
            this.resetForm();
            this.loadFiles();
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

  changeItemsPerPage() {
    this.calculateTotalPages();
  }

  calculateTotalPages() {
    const totalPages = Math.ceil(this.filteredData.length / this.itemsPerPage);
    this.totalPagesArray = Array.from({ length: totalPages }, (_, i) => i + 1);
  }

  filterData() {
    this.currentPage = 1;
    if( !this.searchText || this.searchText.length==0 ){
      this.filteredData = this.data;
    }
    else {
      this.filteredData = this.data.filter((row: any) =>
        row.name.toLowerCase().includes(this.searchText.toLowerCase())
      );
    }
    this.calculateTotalPages();
  }

  changePage(page: number) {
    this.currentPage = page;
  }

  resetForm(){
    if(this.fileInput_el) {
      // @ts-ignore
      this.fileInput_el.nativeElement.value = '';
      this.searchText = '';
    }
  }

}

