import { ErrorHandler, Injectable, NgZone } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import {AlertDialogComponent} from "../components/alert-dialog/alert-dialog.component";

@Injectable({
    providedIn: 'root',
})
export class GlobalErrorHandlerService extends ErrorHandler {
    constructor(private dialog: MatDialog, private ngZone: NgZone) {
        super();
    }

    override handleError(err: any): void {
        this.ngZone.run(() => {
            this.dialog.open(AlertDialogComponent, {
                data: {
                    icon: 'error',
                    message: err.message,
                    buttonText: 'OK',
                    isError: true }
            });
        });
    }
}
