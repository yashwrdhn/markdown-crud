import { Component, OnInit, OnDestroy, ViewChild, ElementRef, HostListener } from '@angular/core';
import { MarkdownFilesService, MarkdownFile } from './markdown.service';

export interface TocEntry {
  id: string;
  text: string;
  level: 2 | 3;
}

@Component({
  selector: 'app-markdown',
  standalone: false,
  templateUrl: './markdown.component.html',
  styleUrl: './markdown.component.css'
})
export class MarkdownComponent implements OnInit, OnDestroy {

  /* ── file list ── */
  files: MarkdownFile[] = [];
  activeFile: MarkdownFile | null = null;

  /* ── content ── */
  markdown = '';
  editBuffer = '';
  isEditing = false;

  /* ── sidebars ── */
  leftOpen = false;
  rightOpen = true;

  /* ── toc ── */
  toc: TocEntry[] = [];
  activeTocId: string = '';
  private observer: IntersectionObserver | null = null;

  /* ── new note ── */
  editingNewLabel = false;
  newNoteLabel = '';

  @ViewChild('scrollHost') scrollHostRef!: ElementRef<HTMLElement>;
  @ViewChild('newLabelInput') newLabelInputRef!: ElementRef<HTMLInputElement>;

  constructor(private markdownFilesService: MarkdownFilesService) { }

  ngOnInit(): void {
    this.markdownFilesService.getFiles().subscribe(files => {
      this.files = files;
      if (files.length > 0) this.openFile(files[0]);
    });
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }

  newFile(): void {
     this.markdownFilesService.createDocument('New File', '').subscribe(document => {
      
     })
  }

  /* alias used by the drawer template */
  selectFile(file: MarkdownFile): void {
    this.openFile(file);
    this.leftOpen = false;
  }

  toggleDrawer(): void {
    this.leftOpen = !this.leftOpen;
  }

  openFile(file: MarkdownFile): void {
  if (this.isEditing) this.cancelEdit();

  this.activeFile = file;

  if (file.content !== undefined) {
    this.markdown = file.content;
    this.scheduleTocBuild();
    return;
  }

  this.markdownFilesService.getDocument(file.uuid).subscribe(document => {
    this.markdown = document.content;

    // Preserve the existing frontend object
    file.content = document.content;

    this.scheduleTocBuild();
  });
}

  /* ── edit ops ── */
  enterEdit(): void {
    this.editBuffer = this.markdown;
    this.isEditing = true;
  }

  saveEdit(): void {
    this.markdown = this.editBuffer;
    if (this.activeFile) this.activeFile.content = this.editBuffer;
    this.isEditing = false;
    this.scheduleTocBuild();
  }

  cancelEdit(): void {
    this.editBuffer = '';
    this.isEditing = false;
  }

  /* ── toolbar actions (textarea markdown wrapping) ── */
  @ViewChild('editorTextarea') editorRef!: ElementRef<HTMLTextAreaElement>;

  wrapSelection(before: string, after: string = before): void {
    const ta = this.editorRef?.nativeElement;
    if (!ta) return;
    const start = ta.selectionStart;
    const end = ta.selectionEnd;
    const sel = this.editBuffer.substring(start, end);
    const replacement = before + (sel || 'text') + after;
    this.editBuffer =
      this.editBuffer.substring(0, start) +
      replacement +
      this.editBuffer.substring(end);
    setTimeout(() => {
      ta.focus();
      ta.selectionStart = start + before.length;
      ta.selectionEnd = start + before.length + (sel || 'text').length;
    });
  }

  insertLine(prefix: string): void {
    const ta = this.editorRef?.nativeElement;
    if (!ta) return;
    const start = ta.selectionStart;
    const lineStart = this.editBuffer.lastIndexOf('\n', start - 1) + 1;
    const lineEnd = this.editBuffer.indexOf('\n', start);
    const end = lineEnd === -1 ? this.editBuffer.length : lineEnd;
    const line = this.editBuffer.substring(lineStart, end);
    // strip existing prefix if same
    const stripped = line.replace(/^#{1,3}\s/, '');
    const newLine = prefix + stripped;
    this.editBuffer =
      this.editBuffer.substring(0, lineStart) +
      newLine +
      this.editBuffer.substring(end);
    setTimeout(() => { ta.focus(); });
  }

  insertLink(): void {
    const url = prompt('URL:');
    if (!url) return;
    this.wrapSelection('[', `](${url})`);
  }

  insertImage(): void {
    const url = prompt('Image URL:');
    if (!url) return;
    const alt = prompt('Alt text:', 'image') ?? 'image';
    const ta = this.editorRef?.nativeElement;
    if (!ta) return;
    const pos = ta.selectionEnd;
    const snippet = `![${alt}](${url})`;
    this.editBuffer =
      this.editBuffer.substring(0, pos) + snippet + this.editBuffer.substring(pos);
    setTimeout(() => { ta.focus(); });
  }

  /* ── new note ── */
  startNewNote(): void {
    this.editingNewLabel = true;
    this.newNoteLabel = '';
    setTimeout(() => this.newLabelInputRef?.nativeElement.focus());
  }

  // confirmNewNote(): void {
  //   const label = this.newNoteLabel.trim() || 'Untitled note';
  //   const template = `# ${label}\n\n*${new Date().toLocaleDateString('en-IN', { day: 'numeric', month: 'long', year: 'numeric' })}*\n\n## Overview\n\nWrite your overview here.\n\n## Notes\n\n`;
  //   const file: MarkdownFile = { label, path: '', content: template };
  //   this.files = [...this.files, file];
  //   this.editingNewLabel = false;
  //   this.openFile(file);
  //   this.enterEdit();
  // }

  cancelNewNote(): void {
    this.editingNewLabel = false;
    this.newNoteLabel = '';
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.editingNewLabel) this.cancelNewNote();
  }

  /* ── ToC ── */
  private scheduleTocBuild(): void {
    setTimeout(() => this.buildToc(), 120);
  }

  buildToc(): void {
    this.observer?.disconnect();
    const host = this.scrollHostRef?.nativeElement;
    if (!host) return;

    const headings = Array.from(
      host.querySelectorAll<HTMLElement>('h2, h3')
    );

    this.toc = headings.map((el, i) => {
      if (!el.id) el.id = `heading-${i}`;
      return {
        id: el.id,
        text: el.textContent ?? '',
        level: (el.tagName === 'H2' ? 2 : 3) as 2 | 3
      };
    });

    if (this.toc.length === 0) return;

    this.observer = new IntersectionObserver(
      entries => {
        const visible = entries
          .filter(e => e.isIntersecting)
          .sort((a, b) => a.boundingClientRect.top - b.boundingClientRect.top);
        if (visible.length > 0) {
          this.activeTocId = visible[0].target.id;
        }
      },
      { root: host, rootMargin: '-10% 0px -80% 0px', threshold: 0 }
    );

    headings.forEach(el => this.observer!.observe(el));
    if (this.toc.length > 0) this.activeTocId = this.toc[0].id;
  }

  scrollToHeading(id: string): void {
    const host = this.scrollHostRef?.nativeElement;
    const el = host?.querySelector(`#${id}`) as HTMLElement;
    if (!el || !host) return;
    host.scrollTo({ top: el.offsetTop - 64, behavior: 'smooth' });
    this.activeTocId = id;
  }

  onMarkdownReady(): void {
    this.scheduleTocBuild();
  }
}