import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ChatMessage } from './chat-message';

// Ollama's /api/chat endpoint accepts a "system" field the same way
// /api/generate does — overrides the model's default system prompt for
// that request, no Modelfile change needed. Switched from /api/generate
// to /api/chat here specifically because it takes a messages[] array
// (system + prior turns + the new one) and applies the model's chat
// template correctly, which is the right way to give it memory of the
// conversation instead of hand-rolling a single prompt string.
const SYSTEM_PROMPT = `Keep answers sharp and crisp — no throat-clearing, no restating
the question, no "I hope this helps". Say only what's needed, then stop.
Prefer plain sentences over bullet lists unless a list is genuinely clearer.
Tone is dry, subtly sarcastic, unbothered — deadpan wit is fine, cruelty isn't.`;

@Injectable({
  providedIn: 'root'
})
export class AiChatService {

  stream(prompt: string, history: ChatMessage[] = [], system: string = SYSTEM_PROMPT): Observable<string> {

    return new Observable(observer => {

      const controller = new AbortController();

      const messages = [
        { role: 'system', content: system },
        ...history.map(m => ({ role: m.role, content: m.content })),
        { role: 'user', content: prompt }
      ];

      fetch('http://localhost:11434/api/chat', {
        method: 'POST',
        signal: controller.signal,
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          model: 'gemma3:4b',
          messages,
          stream: true,
          options: {
            temperature: 0.6
          }
        })
      })
      .then(async response => {

        if (!response.body) {
          observer.error('No response body');
          return;
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();

        let buffer = '';

        while (true) {

          const { done, value } = await reader.read();

          if (done) {
            observer.complete();
            break;
          }

          buffer += decoder.decode(value, { stream: true });

          const lines = buffer.split('\n');
          buffer = lines.pop() ?? '';

          for (const line of lines) {

            if (!line.trim()) {
              continue;
            }

            const json = JSON.parse(line);

            if (json.message?.content) {
              observer.next(json.message.content);
            }

            if (json.done) {
              observer.complete();
              return;
            }
          }
        }
      })
      .catch(err => observer.error(err));

      return () => controller.abort();

    });

  }
}