import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../model/user';
import { JwtHelperService } from '@auth0/angular-jwt';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

 host: string = environment.apiUrl;

  private token: string;
  private loggedInUsername: string;
  private jwtHelper = new JwtHelperService();

  constructor(private http: HttpClient) { }

    public login(user: User): Observable<HttpResponse<User>>{

      return this.http.post<User>(`${this.host}/user/login`, user,
                          {observe: 'response'});
      //il terzo parametro specifica che indietro dovrà arrivarci tutto,
      //incluso il Token e tutto quanto
    }

    public register(user: User): Observable<User>{
      return this.http.post<User>(`${this.host}/user/register`, user);
    }

    public logout(): void{
      this.token = null;
      this.loggedInUsername = null;
      localStorage.removeItem('user');
      localStorage.removeItem('token');
      localStorage.removeItem('users');
    }

    public saveToken(token: string): void{
      this.token = token;
      localStorage.setItem('token', token);
    }

    public addUserToLocalCache(user: User): void{
      localStorage.setItem('user', JSON.stringify(user));
      //trasforma in string le informazioni dell'oggetto
    }

    public getUserFromLocalCache(): User{
     return JSON.parse(localStorage.getItem('user'))
        //parse è il contrario di stringify
    }

    public loadToken(): void{

      this.token = localStorage.getItem('token');

    }

    public getToken(): string{
      return this.token;
    }

    public isLoggedIn(): boolean{

      this.loadToken();
        if(this.token != null && this.token !== ''){

          if(this.jwtHelper.decodeToken(this.token).sub != null || ''){
          //.sub restituisce il subject cioè lo username !
            if(!this.jwtHelper.isTokenExpired(this.token)){  //se non è scauduto...
              this.loggedInUsername = this.jwtHelper.decodeToken(this.token).sub;
              return true;
            }
          }

        }else {
          this.logout();
          return false;
        }
    }

    //il senso dell'observable sta nel fatto che l'operazione che stiamo richiedendo non è banale:
    //una richiesta http fa molte cose nel background, quindi in parole povere stiamo dicendo al browser:
    //fai quello che devi fare e quando lo fai avvisami

  }
  

