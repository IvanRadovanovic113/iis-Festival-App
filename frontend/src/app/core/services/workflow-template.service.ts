import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { 
  WorkflowTemplateRequest, 
  WorkflowTemplateResponse, 
  WorkflowTemplateDetailResponse 
} from '../models/workflow.model';
import { PageResponse } from './offer.service';

@Injectable({
  providedIn: 'root'
})
export class WorkflowTemplateService {
  private readonly apiUrl = '/api/negotiation-manager/workflow-templates';

  constructor(private http: HttpClient) {}

  createTemplate(request: WorkflowTemplateRequest): Observable<WorkflowTemplateDetailResponse> {
    return this.http.post<WorkflowTemplateDetailResponse>(this.apiUrl, request);
  }

  getTemplates(archived: boolean = false, searchTerm?: string, page: number = 0, size: number = 10): Observable<PageResponse<WorkflowTemplateResponse>> {
    let params = new HttpParams()
      .set('archived', archived.toString())
      .set('page', page.toString())
      .set('size', size.toString());

    if (searchTerm && searchTerm.trim()) {
      params = params.set('searchTerm', searchTerm.trim());
    }

    return this.http.get<PageResponse<WorkflowTemplateResponse>>(this.apiUrl, { params });
  }

  getTemplateById(id: number): Observable<WorkflowTemplateDetailResponse> {
    return this.http.get<WorkflowTemplateDetailResponse>(`${this.apiUrl}/${id}`);
  }

  createNewVersion(id: number): Observable<WorkflowTemplateDetailResponse> {
    return this.http.post<WorkflowTemplateDetailResponse>(`${this.apiUrl}/${id}/copy`, {});
  }

  archiveTemplate(id: number): Observable<WorkflowTemplateResponse> {
    return this.http.patch<WorkflowTemplateResponse>(`${this.apiUrl}/${id}/archive`, {});
  }

  // Dodaj ovu metodu u klasu WorkflowTemplateService
  getAllTemplates(): Observable<WorkflowTemplateResponse[]> {
  // Pošto getTemplates koristi paginaciju, ovde pravimo jednostavan poziv
  // ako ti treba SVE bez paginacije, proveri da li imaš endpoint kao /api/.../all
  // Ako nema, koristi ovaj poziv sa velikim size parametrom:
    return this.http.get<WorkflowTemplateResponse[]>(`${this.apiUrl}/all`); 
  // ILI ako nema /all endpoint, koristi:
  // return this.http.get<PageResponse<WorkflowTemplateResponse>>(this.apiUrl + '?size=100').pipe(map(res => res.content));
  }
}