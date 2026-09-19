import {
  AfterViewChecked,
  Component,
  ElementRef,
  HostListener,
  ViewChild
} from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Subscription } from 'rxjs';
import { ChatMessage } from './chat-message';
import { AiChatService } from './ai-chat.service';
import { renderMarkdown } from './markdown.util';

// Short, original lines — rotate one per session so the empty state
// doesn't feel static on repeat visits.
const QUOTES: string[] = [
  'Say less. Mean more.',
  'The best answers start with a real question.',
  'Slow down — the thought is already there.',
  'One thing at a time, all the way through.',
  'Clarity is a kind of quiet.'
];

@Component({
  selector: 'app-ai-chat',
  standalone: false,
  templateUrl: './ai-chat.component.html',
  styleUrl: './ai-chat.component.css'
})
export class AiChatComponent implements AfterViewChecked {

  isOpen = false;
  prompt = '';
  loading = false;
  messages: ChatMessage[] = [];
  quote = this.pickQuote();

  @ViewChild('scrollAnchor') private scrollAnchor?: ElementRef<HTMLDivElement>;
  @ViewChild('promptField') private promptField?: ElementRef<HTMLTextAreaElement>;

  private shouldScroll = false;
  private streamSub?: Subscription;

  constructor(
    private chatService: AiChatService,
    private sanitizer: DomSanitizer
  ) {}

  get hasMessages(): boolean {
    return this.messages.length > 0;
  }

  // renderMarkdown escapes all input before emitting HTML, so it only
  // ever produces the whitelisted tags it writes itself — safe to trust.
  renderedContent(message: ChatMessage): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(renderMarkdown(message.content));
  }

  // Entry point — call this from the trigger, or from a parent via
  // @ViewChild('chat') + chat.open() if you want to launch it from
  // somewhere else entirely (a shortcut, a menu item, etc).
  open(): void {
    this.isOpen = true;
    document.body.style.overflow = 'hidden';
    setTimeout(() => this.promptField?.nativeElement.focus(), 60);
  }

  // Escape always ends the session, not just closes the view — this is
  // a "leave the room" gesture, so the next open starts fresh.
  close(): void {
    this.streamSub?.unsubscribe();
    this.isOpen = false;
    document.body.style.overflow = '';
    this.messages = [];
    this.prompt = '';
    this.loading = false;
    this.quote = this.pickQuote();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.isOpen) {
      this.close();
    }
  }

  send(): void {
    const value = this.prompt.trim();
    if (!value || this.loading) {
      return;
    }

    const history = [...this.messages];

    this.messages.push({ role: 'user', content: value });

    const assistant: ChatMessage = { role: 'assistant', content: '' };
    this.messages.push(assistant);

    this.prompt = '';
    this.loading = true;
    this.shouldScroll = true;

    this.streamSub = this.chatService.stream(value, history).subscribe({

      next: token => {
        assistant.content += token;
        this.shouldScroll = true;
      },

      error: err => {
        console.error(err);
        if (!assistant.content) {
          assistant.content = 'Something went wrong. Try again.';
        }
        this.loading = false;
      },

      complete: () => {
        this.loading = false;
      }

    });
  }

  // Cancels the in-flight response. Unsubscribing runs the teardown in
  // AiChatService, which aborts the underlying fetch.
  stop(): void {
    this.streamSub?.unsubscribe();
    this.loading = false;
  }

  // Enter sends, Shift+Enter makes a new line — standard chat behavior.
  onComposerKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll) {
      this.scrollAnchor?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'end' });
      this.shouldScroll = false;
    }
  }

  private pickQuote(): string {
    return QUOTES[Math.floor(Math.random() * QUOTES.length)];
  }

}