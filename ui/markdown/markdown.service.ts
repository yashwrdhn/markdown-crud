import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface MarkdownFile {
  uuid: string;
  label: string;
  path: string;
  content?: string;
}

export interface MarkdownDocument {
  uuid: string;
  label: string;
  path: string;
  content: string;
}

export interface CreateMarkdownRequest {
  path: string;
  content: string;
}

@Injectable({ providedIn: 'root' })
export class MarkdownFilesService {

  private readonly apiUrl = 'http://localhost:8080/api/documents';

  constructor(private http: HttpClient) {}

  getFiles(): Observable<MarkdownFile[]> {
    return this.http.get<MarkdownFile[]>(this.apiUrl).pipe(
      map(files =>
        files.map(file => ({
          ...file,
          label: this.getLabel(file.path)
        }))
      )
    );
  }

  getDocument(uuid: string): Observable<MarkdownDocument> {
    return this.http
      .get<Omit<MarkdownDocument, 'label'>>(
        `${this.apiUrl}/${uuid}`
      )
      .pipe(
        map(document => ({
          ...document,
          label: this.getLabel(document.path)
        }))
      );
  }

  createDocument(
    path: string,
    content: string
  ): Observable<void> {
    return this.http.post<void>(
      this.apiUrl,
      {
        path,
        content
      }
    );
  }

  private getLabel(path: string): string {
    const fileName = path.split('/').pop() ?? path;

    return fileName.endsWith('.md')
      ? fileName.slice(0, -3)
      : fileName;
  }
}