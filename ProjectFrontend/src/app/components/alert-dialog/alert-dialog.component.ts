import {Component, Inject, OnInit, ViewEncapsulation} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
    MAT_DIALOG_DATA,
    MatDialogActions,
    MatDialogClose,
    MatDialogContent,
    MatDialogRef,
    MatDialogTitle
} from "@angular/material/dialog";
import {MatIconModule} from "@angular/material/icon";
import {MatButtonModule} from "@angular/material/button";
import {faExclamationCircle, faInfoCircle, faTrash} from "@fortawesome/free-solid-svg-icons";
import {FaIconComponent} from "@fortawesome/angular-fontawesome";

@Component({
  selector: 'app-alert-dialog',
  standalone: true,
    imports: [CommonModule, MatDialogActions, MatIconModule, MatDialogContent, MatDialogTitle, MatButtonModule, MatDialogClose, FaIconComponent],
  templateUrl: './alert-dialog.component.html',
  styleUrl: './alert-dialog.component.css'
})
export class AlertDialogComponent implements OnInit{
    message: string = 'An unspecified error has occurred';
    icon = faInfoCircle;
    buttonText = 'Ok';
    title: string = 'Message';

    constructor(
        @Inject(MAT_DIALOG_DATA)
        private data: {
            message: string;
            icon: string;
            buttonText: string;
            isError: boolean;
        },
        private dialogRef: MatDialogRef<AlertDialogComponent>
    ) {
        if (data?.icon)
            if (data.icon === "error")
                this.icon = faExclamationCircle;
        if (data?.message) this.message = data.message;
        if (data?.buttonText) this.buttonText = data.buttonText;
        if (data?.isError) {
            if(data.isError)
                this.title = 'Error'
        }
    }

    closeDialog() {
        this.dialogRef.close();
    }

    ngOnInit(): void {
        this.dialogRef.updateSize('30%');
    }

    protected readonly clearIcon = faTrash;
}

