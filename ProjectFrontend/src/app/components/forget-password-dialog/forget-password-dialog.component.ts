import {Component, Inject, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {FaIconComponent} from "@fortawesome/angular-fontawesome";
import {HttpService} from "../../services/http.service";
import {timeout} from "rxjs";
import {MessageService} from "primeng/api";
import {ToastModule} from "primeng/toast";
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-upload-dialog',
  standalone: true,
    imports: [CommonModule, FaIconComponent, ToastModule, FormsModule],
  templateUrl: './forget-password-dialog.component.html',
  styleUrl: './forget-password-dialog.component.css',
    providers: [MessageService]
})
export class ForgetPasswordDialogComponent implements OnInit{

    inputValue = "";

    constructor(
        private dialogRef: MatDialogRef<ForgetPasswordDialogComponent>,
        private httpService: HttpService,
        private messageService: MessageService) {
    }

    forgetEmail(email: string){
        if(this.inputValue.length === 0) {
            this.messageService.add({
                severity: "error",
                summary: "Error",
                detail: "Enter an email!"
            });
            return;
        }
        this.httpService.forgetPassword(email).pipe(timeout(5000)).subscribe({
            next: value => {
                this.messageService.add({
                    severity: "success",
                    summary: "Success",
                    detail: "Email sent"
                });
            },
            error: err => {
                console.log(err)
                this.messageService.add({
                    severity: "error",
                    summary: "Error",
                    detail: "Email is not valid"
                });
            }
        })
    }

    closeDialog(){
        this.dialogRef.close();
    }

    ngOnInit(): void {
        this.dialogRef.updateSize('30%');
    }

}

