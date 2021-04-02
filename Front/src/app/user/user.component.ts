import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, Subscription } from 'rxjs';
import { NotificationType } from '../enum/notifications-type.enum';
import { Role } from '../enum/role.enum';
import { CustomHttpResponse } from '../model/custom-http-response';
import { User } from '../model/user';
import { AuthenticationService } from '../service/authentication.service';
import { NotificationService } from '../service/notification.service';
import { UserService } from '../service/user.service';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css']
})
export class UserComponent implements OnInit {

private titleSubject = new BehaviorSubject<string>('Users');
public titleAction$ = this.titleSubject.asObservable();
public refreshing: boolean;
public users: User[] = [];
private subscriptions: Subscription[] = [];
public selectedUser: User;
public fileName: string;
public profileImage: File;
public editUser = new User();
private currentUsername: string
private user: User;

  constructor(private userService: UserService, private notificationService: NotificationService,
          private authenticationService: AuthenticationService, private router: Router) { }

  ngOnInit(): void {
    this.user = this.authenticationService.getUserFromLocalCache();
    this.getUsers(true);
  }

  public onLogOut(): void{
    this.authenticationService.logout();
    this.router.navigate(['/login']);
    this.sendNotification(NotificationType.SUCCESS, `You've been logged out succesfully`);
  }

public changeTitle(title: string): void{
  this.titleSubject.next(title);
}

public getUsers(showNotification: boolean): void{
  this.refreshing = true;
  this.subscriptions.push(
    this.userService.getUsers().subscribe(
      (response: User[]) =>{
        //aggiungi gli user alla cache
        this.userService.addUsersToLocalCache(response);
        //popola lista
        this.users = response;
        this.refreshing = false;
        if(showNotification){
          this.sendNotification(NotificationType.SUCCESS, `${response.length} user(s) loaded succesfully`);
        }
      },
      (errorResponse: HttpErrorResponse) => {
        this.sendNotification(NotificationType.ERROR, errorResponse.error.message);
      }
    )
  );
}  

private sendNotification(notificationType: NotificationType, message: string): void{
  if(message)
    this.notificationService.notify(notificationType, message);
  else
    this.notificationService.notify(notificationType, 'An error occurred. Please try again');
}

public onSelectUser(selectedUser: User): void{
  this.selectedUser = selectedUser;
  this.clickButton('openUserInfo'); //va al pulsante, JavaScript purissimo ((((:
}

public onProfileImageChange(fileName: string, file: File): void{
  this.fileName = fileName;
  this.profileImage = file;
  console.log(fileName, file);
}

public saveNewUser(): void{
  this.clickButton('new-user-save');
}

public onAddNewUser(userForm: NgForm): void{
  const formData = this.userService.createUserFormData(null, userForm.value, this.profileImage);
        //primo parametro null perchè non stiamo aggiornando
  this.subscriptions.push(
  this.userService.addUser(formData).subscribe(
    (response: User) => {
      this.clickButton('new-user-close');
      this.getUsers(false); //non mostrerà "Loaded n users"
      //adesso resetta tutto
      this.fileName = null;
      this.profileImage = null;
      userForm.reset();
      this.sendNotification(NotificationType.SUCCESS, `${response.firstName} ${response.lastName} added succesfully`)
    },
    (errorResponse: HttpErrorResponse) => {
      this.sendNotification(NotificationType.ERROR, errorResponse.error.message);
      }
    )
  );
}

public searchUsers(searchTerm: string): void{
  const result: User[] = [];
  for(const user of this.userService.getUsersFromLocalCache()){
    if(user.firstName.toLowerCase().indexOf(searchTerm.toLowerCase()) !== -1
          || user.lastName.toLowerCase().indexOf(searchTerm.toLowerCase()) !== -1
            || user.username.toLowerCase().indexOf(searchTerm.toLowerCase()) !== -1
              || user.userId.toLowerCase().indexOf(searchTerm.toLowerCase()) !== -1){
                result.push(user);
              }
  }

  this.users = result;
  if(result.length === 0 || !searchTerm){
    this.users = this.userService.getUsersFromLocalCache();
  }

}

public onEditUser(editUser: User): void{
  this.editUser = editUser;
  this.currentUsername = editUser.username;
  this.clickButton('openUserEdit');
}

public onUpdateUser(): void{
  const formData = this.userService.createUserFormData(this.currentUsername, this.editUser, this.profileImage);
        //primo parametro null perchè non stiamo aggiornando
  this.subscriptions.push(
  this.userService.updateUser(formData).subscribe(
    (response: User) => {
      this.clickButton('closeEditUserModalButton');
      this.getUsers(false); //non mostrerà "Loaded n users"
      //adesso resetta tutto
      this.fileName = null;
      this.profileImage = null;
      this.sendNotification(NotificationType.SUCCESS, `${response.firstName} ${response.lastName} updated succesfully`)
    },
    (errorResponse: HttpErrorResponse) => {
      this.sendNotification(NotificationType.ERROR, errorResponse.error.message);
      }
    )
  );
}

public onDeleteUser(userId: number): void{
  this.subscriptions.push(
    this.userService.deleteUser(userId).subscribe(
      (response: CustomHttpResponse) => {
        this.sendNotification(NotificationType.SUCCESS, response.message);
        this.getUsers(false);
      },
      (errorResponse: HttpErrorResponse) => {
        this.sendNotification(NotificationType.ERROR, errorResponse.error.message);
      }
    )
  );
}

public onResetPassword(emailForm: NgForm): void{
  this.refreshing = true;
  const emailAddress = emailForm.value[''];
  this.subscriptions.push(
    this.userService.resetPassword(emailAddress).subscribe(
      (response: CustomHttpResponse) => {   //try
        this.sendNotification(NotificationType.SUCCESS, response.message);
        this.getUsers(false);
      },
      (errorResponse: HttpErrorResponse) => {   //catch
        this.sendNotification(NotificationType.ERROR, errorResponse.error.message)
      },
      () => emailForm.reset()   //finally
    )
  );
}

//----------RESTRIZIONE DI RUOLO

public get isAdmin(): boolean{
  return this.getUserRole() === Role.ADMIN || this.getUserRole() === Role.SUPER_ADMIN;
}

public get isManager(): boolean{
  return this.isAdmin || this.getUserRole() === Role.MANAGER;
}

private getUserRole(): string{
  return this.authenticationService.getUserFromLocalCache().role;
}

private clickButton(buttonId: string): void{
  document.getElementById(buttonId).click();
}

}
