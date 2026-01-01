import type { MutationObject } from '../app';
import type { Readable, Subscriber, Unsubscriber } from 'svelte/store';

export function storify<M = unknown>(obj: MutationObject<M>, propertyName: M) {
	return {
		readable<T>(): Readable<T | null> {
			return {
				subscribe(run: Subscriber<T | null>, invalidate?: () => void): Unsubscriber {
					const id = obj.onMutate(propertyName, (value: T) => {
						invalidate?.();
						run(value);
					});

					// @ts-ignore
					obj[propertyName].then((value: T) => run(value));
					run(null);

					return () => obj.offMutate(id);
				}
			};
		}
	};
}

export async function modify(obj: any, propertyName: string, key: string, value: any) {
	const current = await obj[propertyName];
	const newValue = {
		...current,
		[key]: value
	};
	obj[propertyName] = newValue;
}
