import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FontAwesomeModule } from "@fortawesome/angular-fontawesome";
import {faExchangeAlt, faQuestionCircle, faSignOutAlt, faUserCircle} from "@fortawesome/free-solid-svg-icons";
import {Router, RouterLink, RouterOutlet} from "@angular/router";
import {HttpService} from "../../services/http.service";

@Component({
  selector: 'app-dashboard',
  standalone: true,
    imports: [CommonModule, FontAwesomeModule, RouterLink, RouterOutlet],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {
    userIcon = faUserCircle;
    transferIcon = faExchangeAlt;
    aboutIcon = faQuestionCircle;
    logoutIcon = faSignOutAlt;

    constructor(private httpService: HttpService, private router: Router) {

    }

    logout(){
        this.httpService.logout().subscribe({
            next: value => {
                console.log(value);
                sessionStorage.clear();
                this.router.navigate(["/"]);
            },
            error: err => {
                sessionStorage.clear();
                this.router.navigate(["/"]);
            }
        })
    }

}
