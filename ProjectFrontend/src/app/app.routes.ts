import { Routes } from '@angular/router';
import {LoginComponent} from "./components/login/login.component";
import {DashboardComponent} from "./components/dashboard/dashboard.component";
import {FilesComponent} from "./components/files/files.component";
import {UserComponent} from "./components/user/user.component";
import {AboutUsComponent} from "./components/about-us/about-us.component";
import {ChangePasswordComponent} from "./components/change-password/change-password.component";
import {HomepageComponent} from "./components/homepage/homepage.component";
import {NotFoundComponent} from "./components/exceptions/not-found/not-found.component";
import {AuthGuard} from "./guards/auth-guard";

export const routes: Routes = [
    { path: "", component: HomepageComponent},
    { path: "login", component: LoginComponent },
    { path: "dashboard", component: DashboardComponent, children:[
            { path: "files", component: FilesComponent },
            { path: "user", component: UserComponent },
            { path: "about-us", component: AboutUsComponent },
            { path: "change-password", component: ChangePasswordComponent }
    ], canActivate: [AuthGuard]},
    { path: '**', pathMatch: 'full', component: NotFoundComponent }
];
