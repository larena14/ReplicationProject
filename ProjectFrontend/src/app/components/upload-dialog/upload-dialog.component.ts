import {Component, Inject, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {FaIconComponent} from "@fortawesome/angular-fontawesome";
import {HttpService} from "../../services/http.service";
import {catchError, of, timeout} from "rxjs";
import {MessageService} from "primeng/api";
import {ToastModule} from "primeng/toast";
import {
    faCheck,
    faClock,
    faExclamation,
    faExclamationTriangle, faHourglass,
    faRedo,
    faSpinner,
    faTimes,
    faTrash, faUpload
} from "@fortawesome/free-solid-svg-icons";
import {HttpErrorResponse} from "@angular/common/http";

@Component({
  selector: 'app-upload-dialog',
  standalone: true,
    imports: [CommonModule, FaIconComponent, ToastModule],
  templateUrl: './upload-dialog.component.html',
  styleUrl: './upload-dialog.component.css',
    providers: [MessageService]
})
export class UploadDialogComponent implements OnInit{

    title: string = "";
    instanceId: string = "";
    files: FileStatus[] = [];
    clearIcon = faTrash;
    deleteIcon = faTimes;
    initialIcon = faUpload;
    uploadingIcon = faHourglass;
    failIcon = faExclamationTriangle;
    successIcon = faCheck;

    constructor(
        @Inject(MAT_DIALOG_DATA)
        private data: {
            tag: string,
            instanceId: string;
            messageService: MessageService
        },
        private dialogRef: MatDialogRef<UploadDialogComponent>,
        private httpService: HttpService,
        private messageService: MessageService) {
        if(data?.tag) this.title = "Upload file on ["+data.tag+"]";
        if(data?.instanceId) this.instanceId = data.instanceId;
    }

    onUpload() {
        [...this.files].forEach((file) => {
            const reader = new FileReader();
            reader.readAsDataURL(file.file);
            reader.onloadstart = () => {
                file.status = "initial";
            }
            reader.onprogress = () => {
                file.status = "uploading";
            }
            reader.onloadend = () => {
                const result = (reader.result as string).split("base64,")[1];
                this.httpService.putObject(this.instanceId, file.file.name, result).pipe(timeout(30000), catchError( (err : any) => {
                    file.status = "fail";
                    this.messageService.add({
                        severity: "error",
                        summary: "Error",
                        detail: "Unable to upload file ["+file.file.name+"]"
                    })
                    return of(new HttpErrorResponse({status: 500}));
                })).subscribe({
                    next: (value:any)  => {
                        if (value instanceof HttpErrorResponse)
                            file.status = "fail";
                        else
                            file.status = "success";
                    },
                    error: err => {
                        file.status = "fail";
                    }
                });
            }
            reader.onerror = () => {
                file.status = "fail";
            }
        });
    }

    onChange(event: any) {
        const files = event.target.files;

        if (files.length) {
            let tmpFiles: FileStatus[] = [];
            [...files].forEach((file) => {
                tmpFiles.push({
                    file: file,
                    status: "initial"
                })
            })
            this.files = tmpFiles;
        }
    }

    clearFileList(){
        this.files = [];
    }

    removeFileFromList(file: FileStatus){
        const index = this.files.indexOf(file, 0);
        if (index > -1) {
            this.files.splice(index, 1);
        }
    }

    closeDialog(){
        this.dialogRef.close();
    }

    ngOnInit(): void {
        this.dialogRef.updateSize('30%');
    }

    protected readonly refreshIcon = faRedo;
    protected readonly Array = Array;
}

export interface FileStatus{
    file: File,
    status: "initial" | "uploading" | "success" | "fail";
}
